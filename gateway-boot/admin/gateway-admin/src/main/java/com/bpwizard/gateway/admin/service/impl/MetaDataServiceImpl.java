/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * Contributor license agreements.See the NOTICE file distributed with
 * This work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * he License.You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.bpwizard.gateway.admin.service.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.bpwizard.gateway.admin.dto.MetaDataDTO;
import com.bpwizard.gateway.admin.entity.MetaDataDO;
import com.bpwizard.gateway.admin.listener.DataChangedEvent;
import com.bpwizard.gateway.admin.mapper.MetaDataMapper;
import com.bpwizard.gateway.admin.page.CommonPager;
import com.bpwizard.gateway.admin.page.PageParameter;
import com.bpwizard.gateway.admin.query.MetaDataQuery;
import com.bpwizard.gateway.admin.service.MetaDataService;
import com.bpwizard.gateway.admin.transfer.MetaDataTransfer;
import com.bpwizard.gateway.admin.vo.MetaDataVO;
import com.bpwizard.gateway.common.constant.AdminConstants;
import com.bpwizard.gateway.common.dto.MetaData;
import com.bpwizard.gateway.common.enums.ConfigGroupEnum;
import com.bpwizard.gateway.common.enums.DataEventTypeEnum;
import com.bpwizard.gateway.common.utils.UUIDUtils;

/**
 * The type Meta data service.
 */
@Service("metaDataService")
public class MetaDataServiceImpl implements MetaDataService {
    
    private static final Logger logger = LoggerFactory.getLogger(MetaDataServiceImpl.class);
    
    private final MetaDataMapper metaDataMapper;
    
    private final ApplicationEventPublisher eventPublisher;
    
    /**
     * Instantiates a new Meta data service.
     *
     * @param metaDataMapper the meta data mapper
     * @param eventPublisher the event publisher
     */
    @Autowired(required = false)
    public MetaDataServiceImpl(final MetaDataMapper metaDataMapper, final ApplicationEventPublisher eventPublisher) {
        this.metaDataMapper = metaDataMapper;
        this.eventPublisher = eventPublisher;
    }
    
    @Override
    public String createOrUpdate(final MetaDataDTO metaDataDTO) {
        String msg = checkData(metaDataDTO);
        if (StringUtils.isNoneBlank(msg)) {
            return msg;
        }
        MetaDataDO metaDataDO = MetaDataTransfer.INSTANCE.mapToEntity(metaDataDTO);
        DataEventTypeEnum eventType;
        if (StringUtils.isEmpty(metaDataDTO.getId())) {
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            metaDataDO.setId(UUIDUtils.getInstance().generateShortUuid());
            metaDataDO.setDateCreated(currentTime);
            metaDataDO.setDateUpdated(currentTime);
            metaDataMapper.insert(metaDataDO);
            eventType = DataEventTypeEnum.CREATE;
        } else {
            MetaDataDO m = metaDataMapper.selectById(metaDataDTO.getId());
            Optional.ofNullable(m).ifPresent(e -> metaDataDTO.setEnabled(e.getEnabled()));
            metaDataMapper.update(metaDataDO);
            eventType = DataEventTypeEnum.UPDATE;
        }
        // publish AppAuthData's event
        eventPublisher.publishEvent(new DataChangedEvent(ConfigGroupEnum.META_DATA, eventType,
                Collections.singletonList(MetaDataTransfer.INSTANCE.mapToData(metaDataDTO))));
        return StringUtils.EMPTY;
    }
    
    @Override
    @Transactional
    public int delete(final List<String> ids) {
        int count = 0;
        List<MetaData> metaDataList = new ArrayList<>();
        for (String id : ids) {
            MetaDataDO metaDataDO = metaDataMapper.selectById(id);
            count += metaDataMapper.delete(id);
            // publish delete event
            metaDataList.add(MetaDataTransfer.INSTANCE.mapToData(metaDataDO));
        }
        eventPublisher.publishEvent(new DataChangedEvent(ConfigGroupEnum.META_DATA, DataEventTypeEnum.DELETE, metaDataList));
        return count;
    }
    
    @Override
    public String enabled(final List<String> ids, final Boolean enabled) {
        List<MetaData> metaDataList = new ArrayList<>();
        for (String id : ids) {
            MetaDataDO metaDataDO = metaDataMapper.selectById(id);
            if (Objects.isNull(metaDataDO)) {
                return AdminConstants.ID_NOT_EXIST;
            }
            metaDataDO.setEnabled(enabled);
            metaDataMapper.updateEnable(metaDataDO);
            metaDataList.add(MetaDataTransfer.INSTANCE.mapToData(metaDataDO));
        }
        // publish change event.
        eventPublisher.publishEvent(new DataChangedEvent(ConfigGroupEnum.META_DATA, DataEventTypeEnum.UPDATE,
                metaDataList));
        return StringUtils.EMPTY;
    }
    
    @Override
    public void syncData() {
        List<MetaDataDO> all = metaDataMapper.findAll();
        if (!CollectionUtils.isEmpty(all)) {
            eventPublisher.publishEvent(new DataChangedEvent(ConfigGroupEnum.META_DATA, DataEventTypeEnum.REFRESH, MetaDataTransfer.INSTANCE.mapToDataAll(all)));
        }
    }
    
    @Override
    public MetaDataVO findById(final String id) {
        return Optional.ofNullable(MetaDataTransfer.INSTANCE.mapToVO(metaDataMapper.selectById(id))).orElse(new MetaDataVO());
    }
    
    @Override
    public CommonPager<MetaDataVO> listByPage(final MetaDataQuery metaDataQuery) {
        PageParameter pageParameter = metaDataQuery.getPageParameter();
        return new CommonPager<>(
                new PageParameter(pageParameter.getCurrentPage(), pageParameter.getPageSize(),
                        metaDataMapper.countByQuery(metaDataQuery)),
                metaDataMapper.selectByQuery(metaDataQuery)
                        .stream()
                        .map(MetaDataTransfer.INSTANCE::mapToVO)
                        .collect(Collectors.toList()));
    }
    
    @Override
    public List<MetaDataVO> findAll() {
        return MetaDataTransfer.INSTANCE.mapToVOList(metaDataMapper.selectAll());
    }
    
    @Override
    public Map<String, List<MetaDataVO>> findAllGroup() {
        List<MetaDataVO> metaDataVOS = MetaDataTransfer.INSTANCE.mapToVOList(metaDataMapper.selectAll());
        return metaDataVOS.stream().collect(Collectors.groupingBy(MetaDataVO::getAppName));
    }
    
    @Override
    public List<MetaData> listAll() {
        return metaDataMapper.selectAll()
                .stream()
                .filter(Objects::nonNull)
                .map(MetaDataTransfer.INSTANCE::mapToData)
                .collect(Collectors.toList());
    }
    
    private String checkData(final MetaDataDTO metaDataDTO) {
        Boolean success = checkParam(metaDataDTO);
        if (!success) {
            logger.error("metaData create param is error,{}", metaDataDTO.toString());
            return AdminConstants.PARAMS_ERROR;
        }
        final MetaDataDO exist = metaDataMapper.findByPath(metaDataDTO.getPath());
        if (exist != null && !exist.getId().equals(metaDataDTO.getId())) {
            return AdminConstants.DATA_PATH_IS_EXIST;
        }
        return StringUtils.EMPTY;
    }
    
    private Boolean checkParam(final MetaDataDTO metaDataDTO) {
        return !StringUtils.isEmpty(metaDataDTO.getAppName())
                && !StringUtils.isEmpty(metaDataDTO.getPath())
                && !StringUtils.isEmpty(metaDataDTO.getRpcType())
                && !StringUtils.isEmpty(metaDataDTO.getServiceName())
                && !StringUtils.isEmpty(metaDataDTO.getMethodName());
    }
}
