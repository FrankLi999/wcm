/*
 *   Licensed to the Apache Software Foundation (ASF) under one or more
 *   contributor license agreements.  See the NOTICE file distributed with
 *   this work for additional information regarding copyright ownership.
 *   The ASF licenses this file to You under the Apache License, Version 2.0
 *   (the "License"); you may not use this file except in compliance with
 *   the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.bpwizard.gateway.admin.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.bpwizard.gateway.admin.dto.SelectorConditionDTO;
import com.bpwizard.gateway.admin.dto.SelectorDTO;
import com.bpwizard.gateway.admin.entity.PluginDO;
import com.bpwizard.gateway.admin.entity.RuleDO;
import com.bpwizard.gateway.admin.entity.SelectorConditionDO;
import com.bpwizard.gateway.admin.entity.SelectorDO;
import com.bpwizard.gateway.admin.listener.DataChangedEvent;
import com.bpwizard.gateway.admin.mapper.PluginMapper;
import com.bpwizard.gateway.admin.mapper.RuleConditionMapper;
import com.bpwizard.gateway.admin.mapper.RuleMapper;
import com.bpwizard.gateway.admin.mapper.SelectorConditionMapper;
import com.bpwizard.gateway.admin.mapper.SelectorMapper;
import com.bpwizard.gateway.admin.page.CommonPager;
import com.bpwizard.gateway.admin.page.PageParameter;
import com.bpwizard.gateway.admin.query.RuleConditionQuery;
import com.bpwizard.gateway.admin.query.RuleQuery;
import com.bpwizard.gateway.admin.query.SelectorConditionQuery;
import com.bpwizard.gateway.admin.query.SelectorQuery;
import com.bpwizard.gateway.admin.service.SelectorService;
import com.bpwizard.gateway.admin.transfer.ConditionTransfer;
import com.bpwizard.gateway.admin.vo.SelectorConditionVO;
import com.bpwizard.gateway.admin.vo.SelectorVO;
import com.bpwizard.gateway.common.dto.ConditionData;
import com.bpwizard.gateway.common.dto.SelectorData;
import com.bpwizard.gateway.common.enums.ConfigGroupEnum;
import com.bpwizard.gateway.common.enums.DataEventTypeEnum;
import com.bpwizard.gateway.common.enums.PluginEnum;

/**
 * SelectorServiceImpl.
 */
@Service("selectorService")
public class SelectorServiceImpl implements SelectorService {

    private SelectorMapper selectorMapper;

    private SelectorConditionMapper selectorConditionMapper;

    private final RuleMapper ruleMapper;

    private final RuleConditionMapper ruleConditionMapper;

    private final PluginMapper pluginMapper;

    private final ApplicationEventPublisher eventPublisher;

    @Autowired(required = false)
    public SelectorServiceImpl(final SelectorMapper selectorMapper,
                               final SelectorConditionMapper selectorConditionMapper,
                               final PluginMapper pluginMapper,
                               final RuleMapper ruleMapper,
                               final RuleConditionMapper ruleConditionMapper,
                               final ApplicationEventPublisher eventPublisher) {
        this.selectorMapper = selectorMapper;
        this.selectorConditionMapper = selectorConditionMapper;
        this.pluginMapper = pluginMapper;
        this.ruleMapper = ruleMapper;
        this.ruleConditionMapper = ruleConditionMapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public String register(final SelectorDTO selectorDTO) {
        SelectorDO selectorDO = SelectorDO.buildSelectorDO(selectorDTO);
        List<SelectorConditionDTO> selectorConditionDTOs = selectorDTO.getSelectorConditions();
        if (!StringUtils.hasText(selectorDTO.getId())) {
            selectorMapper.insertSelective(selectorDO);
            selectorConditionDTOs.forEach(selectorConditionDTO -> {
                selectorConditionDTO.setSelectorId(selectorDO.getId());
                selectorConditionMapper.insertSelective(SelectorConditionDO.buildSelectorConditionDO(selectorConditionDTO));
            });
        }
        publishEvent(selectorDO, selectorConditionDTOs);
        return selectorDO.getId();
    }

    /**
     * create or update selector.
     *
     * @param selectorDTO {@linkplain SelectorDTO}
     * @return rows
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public int createOrUpdate(final SelectorDTO selectorDTO) {
        int selectorCount;
        SelectorDO selectorDO = SelectorDO.buildSelectorDO(selectorDTO);
        List<SelectorConditionDTO> selectorConditionDTOs = selectorDTO.getSelectorConditions();
        if (!StringUtils.hasText(selectorDTO.getId())) {
            selectorCount = selectorMapper.insertSelective(selectorDO);
            selectorConditionDTOs.forEach(selectorConditionDTO -> {
                selectorConditionDTO.setSelectorId(selectorDO.getId());
                selectorConditionMapper.insertSelective(SelectorConditionDO.buildSelectorConditionDO(selectorConditionDTO));
            });
        } else {
            selectorCount = selectorMapper.updateSelective(selectorDO);
            //delete rule condition then add
            selectorConditionMapper.deleteByQuery(new SelectorConditionQuery(selectorDO.getId()));
            selectorConditionDTOs.forEach(selectorConditionDTO -> {
                selectorConditionDTO.setSelectorId(selectorDO.getId());
                SelectorConditionDO selectorConditionDO = SelectorConditionDO.buildSelectorConditionDO(selectorConditionDTO);
                selectorConditionMapper.insertSelective(selectorConditionDO);
            });
        }
        PluginDO pluginDO = pluginMapper.selectById(selectorDO.getPluginId());
        List<ConditionData> conditionDataList =
                selectorConditionDTOs.stream().map(ConditionTransfer.INSTANCE::mapToSelectorDTO).collect(Collectors.toList());
        // publish change event.
        eventPublisher.publishEvent(new DataChangedEvent(ConfigGroupEnum.SELECTOR, DataEventTypeEnum.UPDATE,
                Collections.singletonList(SelectorDO.transFrom(selectorDO, pluginDO.getName(), conditionDataList))));
        return selectorCount;
    }

    /**
     * delete selectors.
     *
     * @param ids primary key.
     * @return rows
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(final List<String> ids) {
        for (String id : ids) {
    
            SelectorDO selectorDO = selectorMapper.selectById(id);
            PluginDO pluginDO = pluginMapper.selectById(selectorDO.getPluginId());
    
            selectorMapper.delete(id);
            selectorConditionMapper.deleteByQuery(new SelectorConditionQuery(id));
    
            //if divide selector delete
            if (PluginEnum.DIVIDE.getName().equals(pluginDO.getName())) {
                UpstreamCheckService.removeByKey(selectorDO.getName());
            }
    
            //发送删除选择器事件
            eventPublisher.publishEvent(new DataChangedEvent(ConfigGroupEnum.SELECTOR, DataEventTypeEnum.DELETE,
                    Collections.singletonList(SelectorDO.transFrom(selectorDO, pluginDO.getName(), null))));
    
            //清除规则与规则条件
            final List<RuleDO> ruleDOList = ruleMapper.selectByQuery(new RuleQuery(id, null));
            if (!CollectionUtils.isEmpty(ruleDOList)) {
                for (RuleDO ruleDO : ruleDOList) {
                    ruleMapper.delete(ruleDO.getId());
                    ruleConditionMapper.deleteByQuery(new RuleConditionQuery(ruleDO.getId()));
                    //发送删除选择器事件
                    eventPublisher.publishEvent(new DataChangedEvent(ConfigGroupEnum.RULE, DataEventTypeEnum.DELETE,
                            Collections.singletonList(RuleDO.transFrom(ruleDO, pluginDO.getName(), null))));

                }
            }
        }
        return ids.size();
    }

    /**
     * find selector by id.
     *
     * @param id primary key.
     * @return {@linkplain SelectorVO}
     */
    @Override
    public SelectorVO findById(final String id) {
        return SelectorVO.buildSelectorVO(selectorMapper.selectById(id),
                selectorConditionMapper.selectByQuery(
                        new SelectorConditionQuery(id))
                        .stream()
                        .map(SelectorConditionVO::buildSelectorConditionVO)
                        .collect(Collectors.toList()));
    }

    @Override
    public SelectorDO findByName(final String name) {
        return selectorMapper.selectByName(name);
    }

    @Override
    public SelectorData buildByName(final String name) {
        SelectorDO selectorDO = selectorMapper.selectByName(name);
        return buildSelectorData(selectorDO);
    }

    /**
     * find page of selector by query.
     *
     * @param selectorQuery {@linkplain SelectorQuery}
     * @return {@linkplain CommonPager}
     */
    @Override
    public CommonPager<SelectorVO> listByPage(final SelectorQuery selectorQuery) {
        PageParameter pageParameter = selectorQuery.getPageParameter();
        return new CommonPager<>(
                new PageParameter(pageParameter.getCurrentPage(), pageParameter.getPageSize(),
                        selectorMapper.countByQuery(selectorQuery)),
                selectorMapper.selectByQuery(selectorQuery).stream()
                        .map(SelectorVO::buildSelectorVO)
                        .collect(Collectors.toList()));
    }

    @Override
    public List<SelectorData> findByPluginId(final String pluginId) {
        return selectorMapper.findByPluginId(pluginId)
                .stream()
                .map(this::buildSelectorData)
                .collect(Collectors.toList());
    }

    @Override
    public List<SelectorData> listAll() {
        return selectorMapper.selectAll()
                .stream()
                .filter(Objects::nonNull)
                .map(this::buildSelectorData)
                .collect(Collectors.toList());
    }

    private void publishEvent(final SelectorDO selectorDO, final List<SelectorConditionDTO> selectorConditionDTOs) {
        PluginDO pluginDO = pluginMapper.selectById(selectorDO.getPluginId());
        List<ConditionData> conditionDataList =
                selectorConditionDTOs.stream().map(ConditionTransfer.INSTANCE::mapToSelectorDTO).collect(Collectors.toList());
        // publish change event.
        eventPublisher.publishEvent(new DataChangedEvent(ConfigGroupEnum.SELECTOR, DataEventTypeEnum.UPDATE,
                Collections.singletonList(SelectorDO.transFrom(selectorDO, pluginDO.getName(), conditionDataList))));
    }

    private SelectorData buildSelectorData(final SelectorDO selectorDO) {
        // find conditions
        List<ConditionData> conditionDataList = selectorConditionMapper
                .selectByQuery(new SelectorConditionQuery(selectorDO.getId()))
                .stream()
                .filter(Objects::nonNull)
                .map(ConditionTransfer.INSTANCE::mapToSelectorDO)
                .collect(Collectors.toList());
        PluginDO pluginDO = pluginMapper.selectById(selectorDO.getPluginId());
        if (Objects.isNull(pluginDO)) {
            return null;
        }
        return SelectorDO.transFrom(selectorDO, pluginDO.getName(), conditionDataList);
    }

}
