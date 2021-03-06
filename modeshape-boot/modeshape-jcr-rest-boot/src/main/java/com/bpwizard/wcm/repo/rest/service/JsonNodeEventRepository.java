package com.bpwizard.wcm.repo.rest.service;

import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;

import com.bpwizard.wcm.repo.rest.JsonUtils;
import com.bpwizard.wcm.repo.rest.handler.AbstractHandler;
import com.bpwizard.wcm.repo.rest.jcr.model.Syndicator;
import com.bpwizard.wcm.repo.rest.jcr.model.WcmEvent;
import com.bpwizard.wcm.repo.rest.jcr.model.WcmEventEntry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;

@Component
public class JsonNodeEventRepository extends AbstractHandler {

	@Autowired(required = false)
	@Qualifier("modeshapeJdbcTemplate")
	protected JdbcTemplate jdbcTemplate;
	
	private SimpleJdbcInsert simpleJdbcInsert;
	// SELECT ITEMTYPE, NODE_PATH, CONVERT(CONTENT USING utf8) FROM SYN_JSON_NODE_EVENT
	private static final String updateEventSql = "INSERT INTO SYN_JSON_NODE_EVENT(ID, REPOSITORY, WORKSPACE, LIBRARY, NODE_PATH, OPERATION, ITEMTYPE, timeCreated, CONTENT) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE SET LIBRARY=?, NODE_PATH = ?, OPERATION = ?, ITEMTYPE=?, timeCreated=?, CONTENT=?";
	private static final String clearWcmEventsSql = "DELETE SYN_JSON_NODE_EVENT WHERE timeCcreated < ?";
	private static final String selectWcmEventsBeforeSql = "SELECT * FROM SYN_JSON_NODE_EVENT WHERE library= ? and timeCreated > ? and timeCreated < ? ORDER BY timeCreated, NODE_PATH ASC LIMIT ? OFFSET ?";
	private static final String selectCndBeforeSql = "SELECT * FROM SYN_JSON_NODE_EVENT WHERE ITEMTYPE= 'cnd' and timeCreated > ? and timeCreated < ? ORDER BY timeCreated ASC";
	
	@PostConstruct
	private void postConstruct() {
		simpleJdbcInsert = 
			new SimpleJdbcInsert(jdbcTemplate).withTableName("SYN_JSON_NODE_EVENT"); //.usingGeneratedKeyColumns("ID");
	};
	
	public int clearWcmEventBefore(Timestamp timestamp) {
		Object[] params = { timestamp };
	    int[] types = {Types.TIMESTAMP};
	    return jdbcTemplate.update(clearWcmEventsSql, params, types);
	}

	public int addCndEvent(WcmEventEntry event) throws JsonProcessingException {
		return doCndInsert(event);
	}

	public int addWcmEvent(WcmEventEntry event) throws JsonProcessingException {
		return doInsert(event);
	}
	
	public int updateWcmEvent(WcmEventEntry event) throws JsonProcessingException {
		return  doUpsert(event);
	}

	public List<WcmEvent> getCndBefore(
			Syndicator syndicator,
			Timestamp endTimestamp) {
		Object args[] = {syndicator.getLastSyndication(), endTimestamp};
		int argTypes[] = { Types.TIMESTAMP, Types.TIMESTAMP};
		List<WcmEvent> wcmEvents = jdbcTemplate.query(
				selectCndBeforeSql, args, argTypes, new WcmEventRowMapper());
		System.out.println(">>>>>>>>> wcmEvents:" + wcmEvents);
		return wcmEvents;
	}
	
	public List<WcmEvent> getWcmEventBefore(
			Syndicator syndicator,
			Timestamp endTimestamp, 
			int pageIndex,
			int pageSize) {
		List<WcmEvent> wcmEvents = new ArrayList<>();
		
		Object args[] = {"", syndicator.getLastSyndication(), endTimestamp, pageSize, pageIndex};
		int argTypes[] = { Types.VARCHAR, Types.TIMESTAMP, Types.TIMESTAMP, Types.INTEGER, Types.INTEGER};
		wcmEvents.addAll(jdbcTemplate.query(
			selectWcmEventsBeforeSql, args, argTypes, new WcmEventRowMapper()));
			
		for (String library: syndicator.getLibraries()) {
			args[0] = library;
			wcmEvents.addAll(jdbcTemplate.query(
				selectWcmEventsBeforeSql, args, argTypes, new WcmEventRowMapper()));
		}
		return wcmEvents;
	}

	public int[] batchInsert(List<WcmEventEntry> events) {
		MapSqlParameterSource parameters[] = events.stream().map(e -> {
			try {
				return insertParameters(e);
			} catch (JsonProcessingException ex) {
				ex.printStackTrace();
				throw new RuntimeException("Json error", ex);
				
			}
		}).toArray(MapSqlParameterSource[]::new);
        return simpleJdbcInsert.executeBatch(parameters);
    }
	
	public int[] batchUpdate(List<WcmEventEntry> events) throws JsonProcessingException {
		List<Object[]> batchArgs = new ArrayList<>();
		for (WcmEventEntry event: events) {
			InputStream content = JsonUtils.writeValueAsStream(event.getJsonNode());
			Object args[] = {
					event.getId(), event.getRepository(), event.getWorkspace(), event.getLibrary(), 
					event.getNodePath(), event.getOperation().name(), event.getItemType().name(), 
					event.getTimeCreated(), content, event.getLibrary(), event.getNodePath(), 
					event.getOperation().name(), event.getItemType().name(), event.getTimeCreated(), 
					content};
			batchArgs.add(args);
		}
		int[] argTypes = {Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, 
				Types.VARCHAR, Types.VARCHAR, Types.TIMESTAMP, Types.BLOB, Types.VARCHAR, 
				Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.TIMESTAMP, Types.BLOB};
		return jdbcTemplate.batchUpdate(updateEventSql, batchArgs, argTypes);
    }
	
	protected int doInsert(WcmEventEntry event) throws JsonProcessingException {
		MapSqlParameterSource parameters = insertParameters(event);
		 return simpleJdbcInsert.execute(parameters);
	}

	protected int doCndInsert(WcmEventEntry event) throws JsonProcessingException {
		MapSqlParameterSource parameters = insertCndParameters(event);
		 return simpleJdbcInsert.execute(parameters);
	}
	
	protected int doUpsert(WcmEventEntry event) throws JsonProcessingException {
		InputStream content = JsonUtils.writeValueAsStream(event.getJsonNode());
		Object args[] = {event.getId(), event.getRepository(), event.getWorkspace(), event.getLibrary(), 
				event.getNodePath(), event.getOperation().name(), event.getItemType().name(), event.getTimeCreated(), 
				content, event.getLibrary(), event.getNodePath(), event.getOperation().name(), 
				event.getItemType().name(), event.getTimeCreated(), content};
		int[] argTypes = {Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, 
				Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.TIMESTAMP, 
				Types.BLOB, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, 
				Types.VARCHAR, Types.TIMESTAMP, Types.BLOB};
	    return jdbcTemplate.update(updateEventSql, args, argTypes);
	}
	
	protected MapSqlParameterSource insertParameters(WcmEventEntry event) throws JsonProcessingException {
		event.setTimeCreated(new Timestamp(System.currentTimeMillis()));
		
		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("ID", event.getId());
		parameters.addValue("REPOSITORY", event.getRepository());
		parameters.addValue("WORKSPACE", event.getWorkspace());
		parameters.addValue("LIBRARY", event.getLibrary());
	    parameters.addValue("NODE_PATH", event.getNodePath());
	    parameters.addValue("ITEMTYPE", event.getItemType().name());
	    parameters.addValue("OPERATION", event.getOperation().name());
	    parameters.addValue("timeCreated", event.getTimeCreated());
	    parameters.addValue("CONTENT", JsonUtils.writeValueAsStream(event.getJsonNode()));
	    //parameters.addValue("CONTENT", event.getContent());
	    
	    
	    return parameters;
	}
	
	protected MapSqlParameterSource insertCndParameters(WcmEventEntry event) throws JsonProcessingException {
		event.setTimeCreated(new Timestamp(System.currentTimeMillis()));
		
		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("ID", event.getId());
		parameters.addValue("REPOSITORY", event.getRepository());
		parameters.addValue("WORKSPACE", event.getWorkspace());
		parameters.addValue("LIBRARY", event.getLibrary());
	    parameters.addValue("NODE_PATH", event.getNodePath());
	    parameters.addValue("ITEMTYPE", event.getItemType().name());
	    parameters.addValue("OPERATION", event.getOperation().name());
	    parameters.addValue("timeCreated", event.getTimeCreated());
	    //parameters.addValue("CONTENT", JsonUtils.writeValueAsStream(event.getJsonNode()));
	    parameters.addValue("CONTENT", event.getContent());
	    
	    
	    return parameters;
	}
	
	protected Set<String> arrayNodeToStrings(ArrayNode nodes) {
		Set<String> ids = new HashSet<>();
		if (nodes != null) {
			for (int i = 0; i < nodes.size(); i++) {
				ids.add(nodes.get(i).textValue());
			}
		}
		return ids;
		
	}
	
	public class WcmEventRowMapper implements RowMapper<WcmEvent> {
	    @Override
	    public WcmEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
	    	WcmEvent wcmEvent = new WcmEvent();
	 
	    	wcmEvent.setId(rs.getString("ID"));
	    	wcmEvent.setRepository(rs.getString("REPOSITORY"));
	    	wcmEvent.setWorkspace(rs.getString("WORKSPACE"));
	    	wcmEvent.setLibrary(rs.getString("LIBRARY"));
	    	wcmEvent.setNodePath(rs.getString("NODE_PATH"));
	    	wcmEvent.setOperation(WcmEvent.Operation.valueOf(rs.getString("OPERATION")));
	    	wcmEvent.setItemType(WcmEvent.WcmItemType.valueOf(rs.getString("ITEMTYPE")));
	    	wcmEvent.setContent(rs.getBytes("CONTENT"));
	    	return wcmEvent;
	    }
	}
}

