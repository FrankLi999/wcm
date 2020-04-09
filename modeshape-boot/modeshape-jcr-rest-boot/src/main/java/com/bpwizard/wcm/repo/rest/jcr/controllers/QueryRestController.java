package com.bpwizard.wcm.repo.rest.jcr.controllers;

import java.util.Arrays;
import java.util.Collections;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modeshape.jcr.api.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bpwizard.wcm.repo.rest.JsonUtils;
import com.bpwizard.wcm.repo.rest.RestHelper;
import com.bpwizard.wcm.repo.rest.jcr.exception.WcmRepositoryException;
import com.bpwizard.wcm.repo.rest.jcr.model.QueryStatement;
import com.bpwizard.wcm.repo.rest.utils.WcmConstants;
import com.bpwizard.wcm.repo.validation.ValidateString;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@RestController
@RequestMapping(QueryRestController.BASE_URI)
@Validated
public class QueryRestController extends BaseWcmRestController {
	private static final Logger logger = LogManager.getLogger(QueryRestController.class);
	
	public static final String BASE_URI = "/wcm/api/queryStatement";


	@GetMapping(path = "/{repository}/{workspace}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<QueryStatement[]> loadQueryStatements(
		@PathVariable("repository") String repository,
		@PathVariable("workspace") String workspace,
		@RequestParam(name="filter", defaultValue = "") String filter,
	    @RequestParam(name="sort", defaultValue = "asc") 
		@ValidateString(acceptedValues={"asc", "desc"}, message="Sort order can only be asc or desc")
		String sortDirection,
	    @RequestParam(name="pageIndex", defaultValue = "0") int pageIndex,
	    @RequestParam(name="pageSize", defaultValue = "3") @Min(3) @Max(10) int pageSize,
		HttpServletRequest request) 
			throws WcmRepositoryException {
		
		if (logger.isDebugEnabled()) {
			logger.traceEntry();
		}

		QueryStatement[] queryStatements = this.doLoadQueryStatements(repository, workspace, request);
		if ("asc".equals(sortDirection)) {
			Arrays.sort(queryStatements);
		} else if ("desc".equals(sortDirection)) {
			Arrays.sort(queryStatements, Collections.reverseOrder());
		}
		if (logger.isDebugEnabled()) {
			logger.traceExit();
		}
		return ResponseEntity.status(HttpStatus.OK).body(queryStatements);
	}
	
	@PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> createQueryStatement(
			@RequestBody QueryStatement query, HttpServletRequest request) 
			throws WcmRepositoryException {

		if (logger.isDebugEnabled()) {
			logger.traceEntry();
		}
		try {
			String repositoryName = query.getRepository();
			ObjectNode qJson = (ObjectNode) query.toJson();
			try {
				QueryManager qrm = this.repositoryManager.getSession(repositoryName, WcmConstants.DEFAULT_WS).getWorkspace().getQueryManager();
				javax.jcr.query.Query jcrQuery = qrm.createQuery(query.getQuery(), Query.JCR_SQL2);
				QueryResult jcrResult = jcrQuery.execute();
				String columnNames[] = jcrResult.getColumnNames();
				if (columnNames != null && columnNames.length > 0) {
					ArrayNode valueArray = JsonUtils.creatArrayNode();
					for (String value : columnNames) {
						valueArray.add(value);
					}
					qJson.set("bpw:columns", valueArray);	
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			// javax.jcr.query.qom.QueryObjectModel qom = null
			String baseUrl = RestHelper.repositoryUrl(request);
			String path = String.format(WcmConstants.NODE_QUERY_PATH_PATTERN, query.getLibrary(), query.getName());
			
			this.itemHandler.addItem(baseUrl,  repositoryName, WcmConstants.DEFAULT_WS, path, qJson);
			if (this.authoringEnabled) {
				Session session = this.repositoryManager.getSession(repositoryName, WcmConstants.DRAFT_WS);
				session.getWorkspace().clone(WcmConstants.DEFAULT_WS, path, path, true);
			}
			if (logger.isDebugEnabled()) {
				logger.traceExit();
			}
			return ResponseEntity.status(HttpStatus.CREATED).build();
		} catch (WcmRepositoryException e ) {
			throw e;
		} catch (RepositoryException re) { 
			throw new WcmRepositoryException(re);
	    } catch (Throwable t) {
			throw new WcmRepositoryException(t);
		}	
	}

	@PutMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE)
	public void saveQuery(@RequestBody QueryStatement query, HttpServletRequest request) 
			throws WcmRepositoryException {
		if (logger.isDebugEnabled()) {
			logger.traceEntry();
		}
		try {
			String baseUrl = RestHelper.repositoryUrl(request);
			String path = String.format(WcmConstants.NODE_QUERY_PATH_PATTERN, query.getLibrary(), query.getName());
			String repositoryName = query.getRepository();
			ObjectNode qJson = (ObjectNode) query.toJson();
			try {
				QueryManager qrm = this.repositoryManager.getSession(repositoryName, WcmConstants.DEFAULT_WS).getWorkspace().getQueryManager();
				javax.jcr.query.Query jcrQuery = qrm.createQuery(query.getQuery(), Query.JCR_SQL2);
				QueryResult jcrResult = jcrQuery.execute();
				String columnNames[] = jcrResult.getColumnNames();
				if (columnNames != null && columnNames.length > 0) {
					ArrayNode valueArray = JsonUtils.creatArrayNode();
					for (String value : columnNames) {
						valueArray.add(value);
					}
					qJson.set("bpw:columns", valueArray);	
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			this.itemHandler.updateItem(baseUrl, repositoryName, WcmConstants.DEFAULT_WS, path, qJson);
			if (this.authoringEnabled) {
				this.itemHandler.updateItem(baseUrl, repositoryName, WcmConstants.DRAFT_WS, path, qJson);
			}
			if (logger.isDebugEnabled()) {
				logger.traceExit();
			}
		} catch (WcmRepositoryException e ) {
			throw e;
		} catch (RepositoryException re) { 
			throw new WcmRepositoryException(re);
	    } catch (Throwable t) {
			throw new WcmRepositoryException(t);
		}	
	}
	
}