/*
 *   Licensed to the Apache Software Foundation (final ASF) under one or more
 *   contributor license agreements.  See the NOTICE file distributed with
 *   this work for additional information regarding copyright ownership.
 *   The ASF licenses this file to You under the Apache License, Version 2.0
 *   (final the "License"); you may not use this file except in compliance with
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

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bpwizard.gateway.admin.dto.PluginDTO;
import com.bpwizard.gateway.admin.entity.PluginDO;
import com.bpwizard.gateway.admin.entity.RuleDO;
import com.bpwizard.gateway.admin.entity.SelectorDO;
import com.bpwizard.gateway.admin.listener.DataChangedEvent;
import com.bpwizard.gateway.admin.mapper.PluginMapper;
import com.bpwizard.gateway.admin.mapper.RuleConditionMapper;
import com.bpwizard.gateway.admin.mapper.RuleMapper;
import com.bpwizard.gateway.admin.mapper.SelectorConditionMapper;
import com.bpwizard.gateway.admin.mapper.SelectorMapper;
import com.bpwizard.gateway.admin.page.CommonPager;
import com.bpwizard.gateway.admin.page.PageParameter;
import com.bpwizard.gateway.admin.query.PluginQuery;
import com.bpwizard.gateway.admin.query.RuleConditionQuery;
import com.bpwizard.gateway.admin.query.RuleQuery;
import com.bpwizard.gateway.admin.query.SelectorConditionQuery;
import com.bpwizard.gateway.admin.query.SelectorQuery;
import com.bpwizard.gateway.admin.service.PluginService;
import com.bpwizard.gateway.admin.transfer.PluginTransfer;
import com.bpwizard.gateway.admin.vo.PluginVO;
import com.bpwizard.gateway.common.constant.AdminConstants;
import com.bpwizard.gateway.common.dto.PluginData;
import com.bpwizard.gateway.common.enums.ConfigGroupEnum;
import com.bpwizard.gateway.common.enums.DataEventTypeEnum;
import com.bpwizard.gateway.common.enums.PluginRoleEnum;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * PluginServiceImpl.
 */
@Service("pluginService")
public class PluginServiceImpl implements PluginService {

    private final PluginMapper pluginMapper;

    private final SelectorMapper selectorMapper;

    private SelectorConditionMapper selectorConditionMapper;

    private final RuleMapper ruleMapper;

    private final RuleConditionMapper ruleConditionMapper;

    private final ApplicationEventPublisher eventPublisher;

    @Autowired(required = false)
    public PluginServiceImpl(final PluginMapper pluginMapper,
                             final SelectorMapper selectorMapper,
                             final SelectorConditionMapper selectorConditionMapper,
                             final RuleMapper ruleMapper,
                             final RuleConditionMapper ruleConditionMapper,
                             final ApplicationEventPublisher eventPublisher) {
        this.pluginMapper = pluginMapper;
        this.selectorMapper = selectorMapper;
        this.selectorConditionMapper = selectorConditionMapper;
        this.ruleMapper = ruleMapper;
        this.ruleConditionMapper = ruleConditionMapper;
        this.eventPublisher = eventPublisher;
    }

    /**
     * create or update plugin.
     *
     * @param pluginDTO {@linkplain PluginDTO}
     * @return rows
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createOrUpdate(final PluginDTO pluginDTO) {
        final String msg = checkData(pluginDTO);
        if (StringUtils.isNoneBlank(msg)) {
            return msg;
        }
        PluginDO pluginDO = PluginDO.buildPluginDO(pluginDTO);
        DataEventTypeEnum eventType = DataEventTypeEnum.CREATE;
        if (StringUtils.isBlank(pluginDTO.getId())) {
            pluginMapper.insertSelective(pluginDO);
        } else {
            eventType = DataEventTypeEnum.UPDATE;
            pluginMapper.updateSelective(pluginDO);
        }

        // publish change event.
        eventPublisher.publishEvent(new DataChangedEvent(ConfigGroupEnum.PLUGIN, eventType,
                Collections.singletonList(PluginTransfer.INSTANCE.mapToData(pluginDO))));
        return StringUtils.EMPTY;
    }

    private String checkData(final PluginDTO pluginDTO) {
        final PluginDO exist = pluginMapper.selectByName(pluginDTO.getName());
        if (StringUtils.isBlank(pluginDTO.getId())) {
            if (Objects.nonNull(exist)) {
                return AdminConstants.PLUGIN_NAME_IS_EXIST;
            }
        } else {
            if (Objects.isNull(exist) || !exist.getId().equals(pluginDTO.getId())) {
                return AdminConstants.PLUGIN_NAME_NOT_EXIST;
            }
        }
        return StringUtils.EMPTY;
    }

    /**
     * delete plugins.
     *
     * @param ids primary key.
     * @return rows
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String delete(final List<String> ids) {
        for (String id : ids) {
            PluginDO pluginDO = pluginMapper.selectById(id);
            if (Objects.isNull(pluginDO)) {
                return AdminConstants.SYS_PLUGIN_ID_NOT_EXIST;
            }
            // if sys plugin not delete
            if (pluginDO.getRole().equals(PluginRoleEnum.SYS.getCode())) {
                return AdminConstants.SYS_PLUGIN_NOT_DELETE;
            }
            pluginMapper.delete(id);

            final List<SelectorDO> selectorDOList = selectorMapper.selectByQuery(new SelectorQuery(id, null));
            selectorDOList.forEach(selectorDO -> {
                final List<RuleDO> ruleDOS = ruleMapper.selectByQuery(new RuleQuery(selectorDO.getId(), null));
                ruleDOS.forEach(ruleDO -> {
                    ruleMapper.delete(ruleDO.getId());
                    ruleConditionMapper.deleteByQuery(new RuleConditionQuery(ruleDO.getId()));
                });
                selectorMapper.delete(selectorDO.getId());
                selectorConditionMapper.deleteByQuery(new SelectorConditionQuery(selectorDO.getId()));
            });
            // publish change event.
            eventPublisher.publishEvent(new DataChangedEvent(ConfigGroupEnum.PLUGIN, DataEventTypeEnum.DELETE,
                    Collections.singletonList(PluginTransfer.INSTANCE.mapToData(pluginDO))));
        }
        return StringUtils.EMPTY;
    }

    @Override
    public String enabled(final List<String> ids, final Boolean enabled) {
        for (String id : ids) {
            PluginDO pluginDO = pluginMapper.selectById(id);
            if (Objects.isNull(pluginDO)) {
                return AdminConstants.SYS_PLUGIN_ID_NOT_EXIST;
            }
            pluginDO.setDateUpdated(new Timestamp(System.currentTimeMillis()));
            pluginDO.setEnabled(enabled);
            pluginMapper.updateEnable(pluginDO);

            // publish change event.
            eventPublisher.publishEvent(new DataChangedEvent(ConfigGroupEnum.PLUGIN, DataEventTypeEnum.UPDATE,
                    Collections.singletonList(PluginTransfer.INSTANCE.mapToData(pluginDO))));
        }
        return StringUtils.EMPTY;
    }


    /**
     * find plugin by id.
     *
     * @param id primary key.
     * @return {@linkplain PluginVO}
     */
    @Override
    public PluginVO findById(final String id) {
        return PluginVO.buildPluginVO(pluginMapper.selectById(id));
    }

    /**
     * find page of plugin by query.
     *
     * @param pluginQuery {@linkplain PluginQuery}
     * @return {@linkplain CommonPager}
     */
    @Override
    public CommonPager<PluginVO> listByPage(final PluginQuery pluginQuery) {
        PageParameter pageParameter = pluginQuery.getPageParameter();
        return new CommonPager<>(
                new PageParameter(pageParameter.getCurrentPage(), pageParameter.getPageSize(),
                        pluginMapper.countByQuery(pluginQuery)),
                pluginMapper.selectByQuery(pluginQuery).stream()
                        .map(PluginVO::buildPluginVO)
                        .collect(Collectors.toList()));
    }

    @Override
    public List<PluginData> listAll() {
        return pluginMapper.selectAll().stream()
                .map(PluginTransfer.INSTANCE::mapToData)
                .collect(Collectors.toList());
    }
}
