package com.bpwizard.wcm.repo.rest.handler;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.servlet.http.HttpServletRequest;

import org.modeshape.jcr.api.query.Query;
import org.modeshape.web.jcr.RepositoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.bpwizard.spring.boot.commons.exceptions.util.SpringExceptionUtils;
import com.bpwizard.wcm.repo.rest.JsonUtils;
import com.bpwizard.wcm.repo.rest.ModeshapeUtils;
import com.bpwizard.wcm.repo.rest.RestHelper;
import com.bpwizard.wcm.repo.rest.WcmUtils;
import com.bpwizard.wcm.repo.rest.jcr.exception.WcmError;
import com.bpwizard.wcm.repo.rest.jcr.exception.WcmRepositoryException;
import com.bpwizard.wcm.repo.rest.jcr.model.AccessControlEntry;
import com.bpwizard.wcm.repo.rest.jcr.model.ArrayConstraint;
import com.bpwizard.wcm.repo.rest.jcr.model.AuthoringTemplate;
import com.bpwizard.wcm.repo.rest.jcr.model.BaseFormGroup;
import com.bpwizard.wcm.repo.rest.jcr.model.CommonConstraint;
import com.bpwizard.wcm.repo.rest.jcr.model.ContentAreaLayout;
import com.bpwizard.wcm.repo.rest.jcr.model.ContentItem;
import com.bpwizard.wcm.repo.rest.jcr.model.ContentItemProperties;
import com.bpwizard.wcm.repo.rest.jcr.model.ControlField;
import com.bpwizard.wcm.repo.rest.jcr.model.CustomConstraint;
import com.bpwizard.wcm.repo.rest.jcr.model.Footer;
import com.bpwizard.wcm.repo.rest.jcr.model.Form;
import com.bpwizard.wcm.repo.rest.jcr.model.FormColumn;
import com.bpwizard.wcm.repo.rest.jcr.model.FormControl;
import com.bpwizard.wcm.repo.rest.jcr.model.FormRow;
import com.bpwizard.wcm.repo.rest.jcr.model.FormRows;
import com.bpwizard.wcm.repo.rest.jcr.model.FormStep;
import com.bpwizard.wcm.repo.rest.jcr.model.FormSteps;
import com.bpwizard.wcm.repo.rest.jcr.model.FormTab;
import com.bpwizard.wcm.repo.rest.jcr.model.FormTabs;
import com.bpwizard.wcm.repo.rest.jcr.model.JavascriptFunction;
import com.bpwizard.wcm.repo.rest.jcr.model.JsonForm;
import com.bpwizard.wcm.repo.rest.jcr.model.LayoutColumn;
import com.bpwizard.wcm.repo.rest.jcr.model.LayoutRow;
import com.bpwizard.wcm.repo.rest.jcr.model.Library;
import com.bpwizard.wcm.repo.rest.jcr.model.NavBar;
import com.bpwizard.wcm.repo.rest.jcr.model.Navigation;
import com.bpwizard.wcm.repo.rest.jcr.model.NavigationBadge;
import com.bpwizard.wcm.repo.rest.jcr.model.NavigationItem;
import com.bpwizard.wcm.repo.rest.jcr.model.NavigationType;
import com.bpwizard.wcm.repo.rest.jcr.model.NumberConstraint;
import com.bpwizard.wcm.repo.rest.jcr.model.ObjectConstraint;
import com.bpwizard.wcm.repo.rest.jcr.model.PageLayout;
import com.bpwizard.wcm.repo.rest.jcr.model.QueryStatement;
import com.bpwizard.wcm.repo.rest.jcr.model.RenderTemplate;
import com.bpwizard.wcm.repo.rest.jcr.model.RenderTemplateLayoutColumn;
import com.bpwizard.wcm.repo.rest.jcr.model.RenderTemplateLayoutRow;
import com.bpwizard.wcm.repo.rest.jcr.model.ResourceElementRender;
import com.bpwizard.wcm.repo.rest.jcr.model.ResourceMixin;
import com.bpwizard.wcm.repo.rest.jcr.model.ResourceViewer;
import com.bpwizard.wcm.repo.rest.jcr.model.SearchData;
import com.bpwizard.wcm.repo.rest.jcr.model.SidePane;
import com.bpwizard.wcm.repo.rest.jcr.model.SidePanel;
import com.bpwizard.wcm.repo.rest.jcr.model.SiteArea;
import com.bpwizard.wcm.repo.rest.jcr.model.SiteAreaLayout;
import com.bpwizard.wcm.repo.rest.jcr.model.SiteConfig;
import com.bpwizard.wcm.repo.rest.jcr.model.StringConstraint;
import com.bpwizard.wcm.repo.rest.jcr.model.Theme;
import com.bpwizard.wcm.repo.rest.jcr.model.ThemeColors;
import com.bpwizard.wcm.repo.rest.jcr.model.Toolbar;
import com.bpwizard.wcm.repo.rest.jcr.model.VisbleCondition;
import com.bpwizard.wcm.repo.rest.jcr.model.WcmEventEntry;
import com.bpwizard.wcm.repo.rest.jcr.model.WcmLibrary;
import com.bpwizard.wcm.repo.rest.jcr.model.WcmOperation;
import com.bpwizard.wcm.repo.rest.jcr.model.WcmProperties;
import com.bpwizard.wcm.repo.rest.jcr.model.WcmProperty;
import com.bpwizard.wcm.repo.rest.jcr.model.WcmRepository;
import com.bpwizard.wcm.repo.rest.jcr.model.WcmSystem;
import com.bpwizard.wcm.repo.rest.jcr.model.WcmWorkspace;
import com.bpwizard.wcm.repo.rest.jcr.model.WorkflowNode;
import com.bpwizard.wcm.repo.rest.modeshape.model.RestChildType;
import com.bpwizard.wcm.repo.rest.modeshape.model.RestNode;
import com.bpwizard.wcm.repo.rest.modeshape.model.RestNodeType;
import com.bpwizard.wcm.repo.rest.modeshape.model.RestProperty;
import com.bpwizard.wcm.repo.rest.modeshape.model.RestPropertyType;
import com.bpwizard.wcm.repo.rest.modeshape.model.RestRepositories;
import com.bpwizard.wcm.repo.rest.modeshape.model.RestRepositories.Repository;
import com.bpwizard.wcm.repo.rest.modeshape.model.RestWorkspaces;
import com.bpwizard.wcm.repo.rest.modeshape.model.RestWorkspaces.Workspace;
import com.bpwizard.wcm.repo.rest.service.SyndicatorRepository;
import com.bpwizard.wcm.repo.rest.utils.WcmConstants;
import com.bpwizard.wcm.repo.rest.utils.WcmErrors;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
public class WcmRequestHandler {
	private static final Logger logger = LoggerFactory.getLogger(WcmRequestHandler.class);
	@Value("${bpw.modeshape.authoring.enabled:true}")
	protected boolean authoringEnabled = true;

	@Value("${bpw.modeshape.syndication.enabled:true}")
	protected boolean syndicationEnabled = true;
	@Autowired
	protected RestWcmItemHandler wcmItemHandler;

	@Autowired
	protected RepositoryManager repositoryManager;

	@Autowired
	protected RestRepositoryHandler repositoryHandler;

	@Autowired
	protected RestNodeTypeHandler nodeTypeHandler;

	@Autowired
	protected RestServerHandler serverHandler;

	@Autowired
	protected WcmUtils wcmUtils;
	
	@Autowired
	protected SyndicatorRepository syndicationUtils;

	protected ObjectMapper objectMapper = new ObjectMapper();
	
	public Session getSession(String repository, String workspace) throws RepositoryException {
		return wcmItemHandler.getSession(repository, workspace);
	}
	
	public WcmSystem getWcmSystem(
			String repository,
			String workspace, 
			String library,
			String siteConfigName, 
			boolean authoring,
			HttpServletRequest request)
			throws RepositoryException, WcmRepositoryException {

		if (logger.isDebugEnabled()) {
			logger.debug("Entry");
		}
		
		WcmSystem wcmSystem = new WcmSystem();
		if (this.authoringEnabled && authoring) {
			Map<String, WcmOperation[]> operations = this.getWcmOperations(repository, workspace, request);
			wcmSystem.setOperations(operations);
			
			wcmSystem.setAuthoringTemplates(this.getAuthoringTemplates(repository, workspace, request));
			wcmSystem.setFormTemplates(this.getForms(repository, workspace, request));
			wcmSystem.setControlFiels(this.getControlField(repository, workspace, request));
			// wcmSystem.setWcmRepositories(this.getWcmRepositories(request));	

			Map<String, JsonForm[]> authoringTemplateForms = this.getSystemAuthoringTemplateAsJsonForm(repository, workspace, request);
			wcmSystem.setAuthoringTemplateForms(authoringTemplateForms);
			
			Map<String, JsonForm[]> forms = this.doGetSystemFormAsJsonForm(repository, workspace, request);
			wcmSystem.setForms(forms);
			
			QueryStatement[] queryStatements = this.loadQueryStatements(repository, workspace, request);
			wcmSystem.setQueryStatements(queryStatements);
		} else {
			Map<String, JsonForm[]> authoringTemplateForms = this.getApplicationAuthoringTemplateAsJsonForm(repository, workspace, library, request);
			wcmSystem.setAuthoringTemplateForms(authoringTemplateForms);
			Map<String, JsonForm[]> forms = this.doGetApplicationFormAsJsonForm(repository, workspace, library, request);
			wcmSystem.setForms(forms);
		}
		wcmSystem.setWcmRepositories(this.getWcmRepositories(request));	
		wcmSystem.setJcrThemes(this.getTheme(repository, workspace, request));
		Map<String, RenderTemplate> renderTemplates = this.getRenderTemplates(repository, workspace, request);
		wcmSystem.setRenderTemplates(renderTemplates);

		Map<String, ContentAreaLayout> contentAreaLayouts = this.getContentAreaLayouts(repository, workspace,
				request);
		wcmSystem.setContentAreaLayouts(contentAreaLayouts);
		
		SiteConfig siteConfig = this.getSiteConfig(request, repository, workspace, library, siteConfigName);
		String rootSiteArea = siteConfig.getRootSiteArea();
		
		String baseUrl = RestHelper.repositoryUrl(request);
		Navigation[] navigations = this.getNavigations(baseUrl, repository, workspace, library, rootSiteArea);
		wcmSystem.setNavigations(navigations);
		wcmSystem.setSiteConfig(siteConfig);
		
		Map<String, SiteArea> siteAreas = this.getSiteAreas(repository, workspace, library, rootSiteArea, baseUrl);
		wcmSystem.setSiteAreas(siteAreas);
		
		if (logger.isDebugEnabled()) {
			logger.debug("Exit");
		}
		return wcmSystem;
		
	}
	
	public WcmRepository[] getWcmRepositories(HttpServletRequest request) throws WcmRepositoryException {
		if (logger.isDebugEnabled()) {
			logger.debug("Entry");
		}

		String baseUrl = RestHelper.repositoryUrl(request);
		String repositoryUrl = RestHelper.urlFrom(request);
		RestRepositories restRepositories = this.getRepositories(request);
		WcmRepository[] wcmRepositories = restRepositories.getRepositories().stream()
				.map(restReoisitory -> this.toWcmRepository(restReoisitory, repositoryUrl, baseUrl))
				.toArray(WcmRepository[]::new);

		if (logger.isDebugEnabled()) {
			logger.debug("Exit");
		}
		return wcmRepositories;
		
	}
	
	public Map<String, WcmOperation[]> getWcmOperations(
			String repository,
			String workspace, 
			HttpServletRequest request) throws RepositoryException {
		if (logger.isDebugEnabled()) {
			logger.debug("Entry");
		}
		
		String baseUrl = RestHelper.repositoryUrl(request);
		RestNode operationsNode = (RestNode) this.wcmItemHandler.item(baseUrl, repository, workspace,
				String.format(WcmConstants.NODE_ROOT_REL_PATH_PATTERN, WcmConstants.OPERATION_REL_PATH), WcmConstants.OPERATION_DEFAULT);
		Map<String, WcmOperation[]> wcmOperationMap = operationsNode.getChildren().stream()
				.filter(node -> WcmUtils.checkNodeType(node, "bpw:supportedOpertions"))
				.map(this::supportedOpertionsToWcmOperation)
				.collect(Collectors.toMap(wcmOperations -> wcmOperations[0].getJcrType(), Function.identity()));
		if (logger.isDebugEnabled()) {
			logger.debug("Exit");
		}
		return wcmOperationMap;
		
	}
	
	public Theme[] getTheme(
			String repository, 
			String workspace,
			HttpServletRequest request) throws WcmRepositoryException {
		if (logger.isDebugEnabled()) {
			logger.debug("Entry");
		}
		String baseUrl = RestHelper.repositoryUrl(request);
		Theme[] themes = this.getThemeLibraries(repository, workspace, baseUrl)
				.flatMap(theme -> this.getThemes(theme, baseUrl)).toArray(Theme[]::new);

		if (logger.isDebugEnabled()) {
			logger.debug("Exit");
		}
		return themes;
	}

	private Stream<Theme> getThemeLibraries(String repository, String workspace, String baseUrl)
			throws WcmRepositoryException {
		try {
			RestNode bpwizardNode = (RestNode) this.wcmItemHandler.item(baseUrl, repository, workspace,
					WcmConstants.NODE_ROOT_PATH, WcmConstants.READ_DEPTH_DEFAULT);
			return bpwizardNode.getChildren().stream().filter(this::isLibrary).filter(this::notSystemLibrary)
					.map(node -> toThemeWithLibrary(node, repository, workspace));
		} catch (RepositoryException e) {
			logger.error("Failed to get theme", e);
			throw new WcmRepositoryException(e, new WcmError(e.getMessage(), WcmErrors.GET_NODE_ERROR, null));
		}
	}

	private Stream<Theme> getThemes(Theme theme, String baseUrl) throws WcmRepositoryException {
		try {
			RestNode themeNode = (RestNode) this.wcmItemHandler.item(baseUrl, theme.getRepositoryName(), theme.getWorkspace(),
					String.format(WcmConstants.NODE_THEME_ROOT_PATH_PATTERN, theme.getLibrary()), WcmConstants.READ_DEPTH_DEFAULT);
			return themeNode.getChildren().stream().filter(this::isTheme).map(node -> this.toTheme(node, theme));
		} catch (RepositoryException e) {
			logger.error("Failed to get theme", e);
			throw new WcmRepositoryException(e, new WcmError(e.getMessage(), WcmErrors.GET_THEME_ERROR, null));

		}
	}

	private Theme toThemeWithLibrary(RestNode node, String repository, String workspace) {
		Theme themeWithLibrary = new Theme();
		themeWithLibrary.setRepositoryName(repository);
		themeWithLibrary.setWorkspace(workspace);
		themeWithLibrary.setLibrary(node.getName());
		return themeWithLibrary;
	}

	private Theme toTheme(RestNode restNode, Theme theme) {
		Theme result = new Theme();
		result.setRepositoryName(theme.getRepositoryName());
		result.setWorkspace(theme.getWorkspace());
		result.setLibrary(theme.getLibrary());
		result.setName(restNode.getName());
		restNode.getChildren().forEach(node -> {
			if (WcmUtils.isElementFolderNode(node)) {
				for (RestProperty restProperty : node.getJcrProperties()) {
					if ("note".equals(restProperty.getName())) {
						result.setNote(restProperty.getValues().get(0));
						break;
					} 
				}
			} else if (WcmUtils.isPropertyFolderNode(node)) {
				for (RestProperty restProperty : node.getJcrProperties()) {
					if ("bpw:title".equals(restProperty.getName())) {
						result.setTitle(restProperty.getValues().get(0));
						break;
					}
				}
			} 
		});
		return result;
	}
	
	private WcmRepository toWcmRepository(Repository restReoisitory, String repositoryUrl, String baseUrl) throws WcmRepositoryException {
		try {
			WcmRepository wcmRepository = new WcmRepository();
			wcmRepository.setName(restReoisitory.getName());
			RestWorkspaces restWorkspaces = this.repositoryHandler.getWorkspaces(repositoryUrl, restReoisitory.getName());
			WcmWorkspace[] wcmWorkspaces = restWorkspaces.getWorkspaces().stream()
					.map(workspace -> toWcmWorkspace(restReoisitory.getName(), workspace, baseUrl))
					.toArray(WcmWorkspace[]::new);
			wcmRepository.setWorkspaces(wcmWorkspaces);
			return wcmRepository;
		} catch (RepositoryException e) {
			logger.error("Failed to get repository", e);
			throw new WcmRepositoryException(e, new WcmError(e.getMessage(), WcmErrors.GET_REPO_ERROR, null));
		}		
	}
    
	private WcmWorkspace toWcmWorkspace(String repository, Workspace restWorkspace, String baseUrl) {
		WcmWorkspace wcmWorkspace = new WcmWorkspace();
		wcmWorkspace.setName(restWorkspace.getName());
		wcmWorkspace.setLibraries(this.getWcmLibraries(repository, wcmWorkspace.getName(), baseUrl));
		return wcmWorkspace;
	}
    
	private WcmLibrary[] getWcmLibraries(String repository, String workspace,
			String baseUrl) throws WcmRepositoryException {
		try {
			RestNode bpwizardNode = (RestNode) this.wcmItemHandler.item(baseUrl, repository, workspace,
					WcmConstants.NODE_ROOT_PATH, WcmConstants.READ_DEPTH_DEFAULT);
			return bpwizardNode.getChildren().stream().filter(this::isLibrary).filter(this::notSystemLibrary)
					.map(node -> {
						 WcmLibrary  wcmLibrary = new  WcmLibrary();
						 wcmLibrary.setName(node.getName());
						 return wcmLibrary;
					}).toArray(WcmLibrary[]::new);
		} catch (RepositoryException e) {
			logger.error("Failed to get library", e);
			throw new WcmRepositoryException(e, new WcmError(e.getMessage(), WcmErrors.GET_LIBS_ERROR, null));

		}
	}
	
	/**
     * Returns the list of JCR repositories available on this server
     *
     * @param request the servlet request; may not be null
     * @return a list of available JCR repositories, as a {@link RestRepositories} instance.
     */
    private RestRepositories getRepositories(
    		HttpServletRequest request 
    		) {
        RestRepositories repositories = new RestRepositories();
        for (String repositoryName : this.repositoryManager.getJcrRepositoryNames()) {
        	String workspacesUrl = RestHelper.urlFrom(request, repositoryName);
            String backupUrl = RestHelper.urlFrom(request, repositoryName, RestHelper.BACKUP_METHOD_NAME);
            String restoreUrl = RestHelper.urlFrom(request, repositoryName, RestHelper.RESTORE_METHOD_NAME);
        	this.serverHandler.addRepository(workspacesUrl, backupUrl, restoreUrl, repositories, repositoryName);
        }
        return repositories;
    }
    
	
	protected WcmOperation[] supportedOpertionsToWcmOperation(final RestNode node) {
		return node.getChildren().stream().filter(n -> WcmUtils.checkNodeType(n, "bpw:supportedOpertion"))
			.map(n -> {
				WcmOperation wcmOperation = new WcmOperation();
				wcmOperation.setJcrType(this.getJcrType(node));
				for (RestProperty property: n.getJcrProperties()) {
					if ("bpw:defaultIcon".equals(property.getName())) {
						wcmOperation.setDefaultIcon(property.getValues().get(0));
					} else if ("bpw:operation".equals(property.getName())) {
						wcmOperation.setOperation(property.getValues().get(0));
					} else if ("bpw:resourceName".equals(property.getName())) {
						wcmOperation.setResourceName(property.getValues().get(0));
					} else if ("bpw:defaultTitle".equals(property.getName())) {
						wcmOperation.setDefaultTitle(property.getValues().get(0));
					} else if ("bpw:condition".equals(property.getName())) {
						wcmOperation.setCondition(property.getValues().get(0));
					}
				}
				return wcmOperation;
			}).toArray(WcmOperation[]::new);
	}
	
	public AuthoringTemplate getAuthoringTemplate(String repository, String workspace, String atPath,
			HttpServletRequest request) throws WcmRepositoryException {
		if (logger.isDebugEnabled()) {
			logger.debug("Entry");
		}
		String baseUrl = RestHelper.repositoryUrl(request);
		AuthoringTemplate at = this.wcmUtils.getAuthoringTemplate(repository, workspace, atPath, baseUrl);
		if (logger.isDebugEnabled()) {
			logger.debug("Exit");
		}
		return at;
	}

	public AuthoringTemplate getAuthoringTemplate(String repository, String workspace, String atPath,
			String baseUrl) throws WcmRepositoryException {
		if (logger.isDebugEnabled()) {
			logger.debug("Entry");
		}
		AuthoringTemplate at = this.wcmUtils.getAuthoringTemplate(repository, workspace, atPath, baseUrl);
		if (logger.isDebugEnabled()) {
			logger.debug("Exit");
		}
		return at;
	}

	
	public Form getForm(String repository, String workspace, String atPath, HttpServletRequest request)
			throws WcmRepositoryException {
		String baseUrl = RestHelper.repositoryUrl(request);
		Form form = this.wcmUtils.getForm(repository, workspace, atPath, baseUrl);
		return form;
	}

	public void lock(String repository, String workspace, String absPath) throws RepositoryException {

		javax.jcr.lock.LockManager lm = this.repositoryManager.getSession(repository, workspace).getWorkspace()
				.getLockManager();
		if (!lm.isLocked(absPath)) {
			lm.lock(absPath, true, false, Long.MAX_VALUE, ModeshapeUtils.getUserName());
		}
	}

	protected boolean isControlField(RestNode node) {
		return WcmUtils.checkNodeType(node, "bpw:controlField");
	}

	protected boolean isSiteArea(RestNode node) {
		return WcmUtils.checkNodeType(node, "bpw:system_siteAreaType");
	}

	protected boolean isSitePage(RestNode node) {
		return WcmUtils.checkNodeType(node, "bpw:system_siteAreaType") && WcmUtils.showOnMenu(node);
	}
	
	public boolean isSiteConfig(RestNode node) {
		return WcmUtils.checkNodeType(node, "bpw:system_siteConfigType");
	}

//	protected boolean isControlFieldMetaData(RestNode node) {
//		return WcmUtils.checkNodeType(node, "bpw:controlFieldMetaData");
//	}

	public boolean isLibrary(RestNode node) {
		return WcmUtils.checkNodeType(node, "bpw:system_libraryType");
	}

	public boolean notSystemLibrary(RestNode node) {
		return WcmUtils.checkNodeType(node, "bpw:system_libraryType") && (!"system".equalsIgnoreCase(node.getName()));
	}

	public boolean isTheme(RestNode node) {
		return WcmUtils.checkNodeType(node, "bpw:system_themeType");
	}

	public boolean isRenderTemplate(RestNode node) {
		return WcmUtils.checkNodeType(node, "bpw:renderTemplate");
	}

	public boolean isContentAreaLayout(RestNode node) {
		return WcmUtils.checkNodeType(node, "bpw:contentAreaLayout");
	}

	public boolean isAuthortingTemplate(RestNode node) {
		return WcmUtils.checkNodeType(node, "bpw:authoringTemplate");
	}

	public boolean isForm(RestNode node) {
		return WcmUtils.checkNodeType(node, "bpw:formType");
	}

	public boolean isBpmnWorkflow(RestNode node) {
		return WcmUtils.checkNodeType(node, "bpw:system_bpmnWorkflowType");
	}

	public boolean isQueryStatement(RestNode node) {
		return WcmUtils.checkNodeType(node, "bpw:system_queryStatementType");
	}

	public boolean isValidationRule(RestNode node) {
		return WcmUtils.checkNodeType(node, "bpw:system_validationRuleType");
	}

	public boolean filterLibrary(Library library, String filter) {
		return StringUtils.hasText(filter) ? library.getName().startsWith(filter) : true;
	}

	protected JsonForm[] toJsonForms(HttpServletRequest request, Form form) throws WcmRepositoryException {
		return new JsonForm[] { this.toJsonForm(request, form, false), this.toJsonForm(request, form, true) };
	}
	
	protected Map<String, JsonForm[]> doGetSystemFormAsJsonForm(String repository, String workspace,
			HttpServletRequest request) throws WcmRepositoryException {

		String baseUrl = RestHelper.repositoryUrl(request);
		Map<String, JsonForm[]> jsonForms = this.getFormLibraries(repository, workspace, baseUrl)
				.flatMap(form -> this.getForms(form, baseUrl)).map(form -> this.toJsonForms(request, form))
				.collect(Collectors.toMap(jfs -> String.format(WcmConstants.WCM_FORM_PATH_PATTERN, jfs[0].getLibrary(),
						jfs[0].getResourceType()), Function.identity()));

		return jsonForms;
	}

	protected Map<String, JsonForm[]> doGetApplicationFormAsJsonForm(String repository, String workspace, String library,
			HttpServletRequest request) throws WcmRepositoryException {

		String baseUrl = RestHelper.repositoryUrl(request);
		Form libraryForm = new Form();
		libraryForm.setRepository(repository);
		libraryForm.setWorkspace(workspace);
		libraryForm.setLibrary(library);
		Map<String, JsonForm[]> jsonForms = this.getForms(libraryForm, baseUrl)
				.map(form -> this.toJsonForms(request, form))
				.collect(Collectors.toMap(jfs -> String.format(WcmConstants.WCM_FORM_PATH_PATTERN, jfs[0].getLibrary(),
						jfs[0].getResourceType()), Function.identity()));

		return jsonForms;
	}
	
	public Map<String, JsonForm[]> getSystemAuthoringTemplateAsJsonForm(String repository, String workspace,
			HttpServletRequest request) throws WcmRepositoryException {

		String baseUrl = RestHelper.repositoryUrl(request);
		Map<String, JsonForm[]> jsonForms = this.getAuthoringTemplateLibraries(repository, workspace, baseUrl)
				.flatMap(at -> this.getAuthoringTemplates(at, baseUrl)).map(at -> this.toJsonForms(request, at))
				.collect(Collectors.toMap(jfs -> String.format(WcmConstants.WCM_AT_PATH_PATTERN, jfs[0].getLibrary(),
						jfs[0].getResourceType()), Function.identity()));

		return jsonForms;
	}

	public Map<String, JsonForm[]> getApplicationAuthoringTemplateAsJsonForm(String repository, String workspace, String library,
			HttpServletRequest request) throws WcmRepositoryException {

		String baseUrl = RestHelper.repositoryUrl(request);
		AuthoringTemplate libraryAt = new AuthoringTemplate();
		libraryAt.setRepository(repository);
		libraryAt.setWorkspace(workspace);
		libraryAt.setLibrary(library);
		Map<String, JsonForm[]> jsonForms = this.getAuthoringTemplates(libraryAt, baseUrl)
				.map(at -> this.toJsonForms(request, at))
				.collect(Collectors.toMap(jfs -> String.format(WcmConstants.WCM_AT_PATH_PATTERN, jfs[0].getLibrary(),
						jfs[0].getResourceType()), Function.identity()));

		return jsonForms;
	}

	protected Stream<AuthoringTemplate> getAuthoringTemplateLibraries(String repository, String workspace,
			String baseUrl) throws WcmRepositoryException {
		try {
			RestNode bpwizardNode = (RestNode) this.wcmItemHandler.item(baseUrl, repository, workspace,
					WcmConstants.NODE_ROOT_PATH, WcmConstants.READ_DEPTH_DEFAULT);
			return bpwizardNode.getChildren().stream().filter(this::isLibrary)
					.map(node -> toAuthoringTemplateWithLibrary(node, repository, workspace));
		} catch (RepositoryException e) {
			throw new WcmRepositoryException(e, new WcmError(e.getMessage(), WcmErrors.GET_NODE_ERROR, new String[] {baseUrl}));
		}
	}

	protected Stream<Form> getFormLibraries(String repository, String workspace, String baseUrl)
			throws WcmRepositoryException {
		try {
			RestNode bpwizardNode = (RestNode) this.wcmItemHandler.item(baseUrl, repository, workspace,
					WcmConstants.NODE_ROOT_PATH, WcmConstants.READ_DEPTH_DEFAULT);
			return bpwizardNode.getChildren().stream().filter(this::isLibrary)
					.map(node -> toFormWithLibrary(node, repository, workspace));
		} catch (RepositoryException e) {
			throw new WcmRepositoryException(e, new WcmError(e.getMessage(), WcmErrors.GET_NODE_ERROR, new String[] {baseUrl}));
		}
	}

	protected AuthoringTemplate toAuthoringTemplateWithLibrary(RestNode node, String repository, String workspace) {
		AuthoringTemplate authoringTemplateWithLibrary = new AuthoringTemplate();
		authoringTemplateWithLibrary.setRepository(repository);
		authoringTemplateWithLibrary.setWorkspace(workspace);
		authoringTemplateWithLibrary.setLibrary(node.getName());
		return authoringTemplateWithLibrary;
	}

	protected Form toFormWithLibrary(RestNode node, String repository, String workspace) {
		Form formWithLibrary = new Form();
		formWithLibrary.setRepository(repository);
		formWithLibrary.setWorkspace(workspace);
		formWithLibrary.setLibrary(node.getName());
		return formWithLibrary;
	}

	protected void resolveWorkflowNode(WorkflowNode workflowNode, RestNode restNode) {
		for (RestProperty property : restNode.getJcrProperties()) {
			if ("publishDate".equals(property.getName())) {
				workflowNode.setPublishDate(property.getValues().get(0));
			} else if ("expireDate".equals(property.getName())) {
				workflowNode.setExpireDate(property.getValues().get(0));
			} else if ("workflow".equals(property.getName())) {
				workflowNode.setWorkflow(property.getValues().get(0));
			} else if ("workflowStage".equals(property.getName())) {
				workflowNode.setWorkflowStage(property.getValues().get(0));
			} else if ("processInstanceId".equals(property.getName())) {
				workflowNode.setProcessInstanceId(property.getValues().get(0));
			} else if ("reviewer".equals(property.getName())) {
				workflowNode.setReviewer(property.getValues().get(0));
			} else if ("editor".equals(property.getName())) {
				workflowNode.setEditor(property.getValues().get(0));
			}
		}
	}

	protected JsonForm[] toJsonForms(HttpServletRequest request, AuthoringTemplate at) throws WcmRepositoryException {
		return new JsonForm[] { this.toJsonForm(request, at, false), this.toJsonForm(request, at, true) };
	}

	protected JsonForm toJsonForm(HttpServletRequest request, Form form, boolean editMode) throws WcmRepositoryException {
		try {
			JsonForm jsonForm = new JsonForm();
			jsonForm.setRepository(form.getRepository());
			jsonForm.setWorkspace(form.getWorkspace());
			jsonForm.setLibrary(form.getLibrary());
			jsonForm.setResourceType(form.getName());
			ObjectNode jsonNode = JsonNodeFactory.instance.objectNode();
			ObjectNode schemaNode = JsonNodeFactory.instance.objectNode();
			jsonNode.set("schema", schemaNode);
			schemaNode.put("type", "object");

			if (StringUtils.hasText(form.getName())) {
				schemaNode.put("name", form.getName());
			}

			if (StringUtils.hasText(form.getTitle())) {
				schemaNode.put("title", form.getTitle());
			}

			if (StringUtils.hasText(form.getDescription())) {
				schemaNode.put("description", form.getDescription());
			}

			Map<String, String> definitionMap = new HashMap<>();

			ObjectNode properties = this.objectMapper.createObjectNode();
			ObjectNode definitions = this.objectMapper.createObjectNode();
			
			if (form.getFormControls() != null && form.getFormControls().size() > 0) {
				ObjectNode elementPropertiesNode = this.objectMapper.createObjectNode();
				elementPropertiesNode.put("type", "object");
				this.popluateFormControls(
						request, 
						form.getRepository(), 
						form.getWorkspace(), 
						properties,
						definitions, 
						form.getFormControls(), 
						editMode, 
						definitionMap);
				String requiredElements[] = form.getFormControls().entrySet().stream().map(entry -> entry.getValue())
						.filter(formControl -> formControl.isMandatory()).map(formControl -> formControl.getName())
						.toArray(String[]::new);
				if (requiredElements != null && requiredElements.length > 0) {
					ArrayNode reqiuriedNode = this.objectMapper.createArrayNode();
					for (String name : requiredElements) {
						reqiuriedNode.add(name);
					}
					elementPropertiesNode.set("required", reqiuriedNode);
				}
			}

			schemaNode.set("properties", properties);
			if (definitionMap.size() > 0) {
				schemaNode.set("definitions", definitions);
			}

			ArrayNode layoutArray = this.toFormLayoutNode(form);
			if (layoutArray != null && layoutArray.size() > 0) {
				jsonNode.set("layout", layoutArray);
			}
			jsonForm.setFormSchema(jsonNode);
			return jsonForm;
		} catch (RepositoryException e) {
			throw new WcmRepositoryException(e, new WcmError(e.getMessage(), WcmErrors.JSON_FORM_ERROR, new String[] {form.getName()}));
		}
	}
	
	protected JsonForm toJsonForm(HttpServletRequest request, AuthoringTemplate at, boolean editMode)
			throws WcmRepositoryException {
		try {
			JsonForm jsonForm = new JsonForm();
			jsonForm.setRepository(at.getRepository());
			jsonForm.setWorkspace(at.getWorkspace());
			jsonForm.setLibrary(at.getLibrary());
			jsonForm.setResourceType(at.getName());
			jsonForm.setNodeType(at.getNodeType());
			ObjectNode jsonNode = JsonNodeFactory.instance.objectNode();
			ObjectNode schemaNode = JsonNodeFactory.instance.objectNode();
			jsonNode.set("schema", schemaNode);
			schemaNode.put("type", "object");

			if (StringUtils.hasText(at.getName())) {
				schemaNode.put("name", at.getName());
			}

			if (StringUtils.hasText(at.getTitle())) {
				schemaNode.put("title", at.getTitle());
			}

			if (StringUtils.hasText(at.getDescription())) {
				schemaNode.put("description", at.getDescription());
			}

			Map<String, String> definitionMap = new HashMap<>();

			ObjectNode properties = this.objectMapper.createObjectNode();
			ObjectNode definitions = this.objectMapper.createObjectNode();
			if (at.getProperties() != null) {
				ObjectNode propertyPropertiesNode = this.objectMapper.createObjectNode();
				propertyPropertiesNode.put("type", "object");
				ObjectNode propertyProperties = this.objectMapper.createObjectNode();
				propertyPropertiesNode.set("properties", propertyProperties);
				properties.set("properties", propertyPropertiesNode);
				Map<String, FormControl> atProperties = new HashMap<>();
				// atProperties.put("authoringTemplate", at.getProperties().getAuthoringTemplate());
				if (at.getProperties().getCategories() != null) {
					atProperties.put("categories", at.getProperties().getCategories());
				}
				// atProperties.put("nodeType", at.getProperties().getNodeType());
				if (at.getProperties().getWorkflow() != null) {
					atProperties.put("workflow", at.getProperties().getWorkflow());
				}
				if (at.getProperties().getName() != null) {
					atProperties.put("name", at.getProperties().getName());
				}
				if (at.getProperties().getTitle() != null) {
				    atProperties.put("title", at.getProperties().getTitle());
				}
				if (at.getProperties().getDescription() != null) {
					atProperties.put("description", at.getProperties().getDescription());
				}
				this.popluateFormControls(request, at.getRepository(), at.getWorkspace(), propertyProperties,
						definitions, atProperties, editMode, definitionMap);
				String requiredProperties[] = atProperties.entrySet().stream().map(entry -> entry.getValue())
						.filter(formControl -> formControl.isMandatory()).map(formControl -> formControl.getName())
						.toArray(String[]::new);
				if (requiredProperties != null && requiredProperties.length > 0) {
					ArrayNode reqiuriedNode = this.objectMapper.createArrayNode();
					for (String name : requiredProperties) {
						reqiuriedNode.add(name);
					}
					propertyPropertiesNode.set("required", reqiuriedNode);
				}
			}

			if (at.getElements() != null && at.getElements().size() > 0) {
				ObjectNode elementPropertiesNode = this.objectMapper.createObjectNode();
				elementPropertiesNode.put("type", "object");
				ObjectNode elementProperties = this.objectMapper.createObjectNode();
				elementPropertiesNode.set("properties", elementProperties);
				properties.set("elements", elementPropertiesNode);
				this.popluateFormControls(request, at.getRepository(), at.getWorkspace(), elementProperties,
						definitions, at.getElements(), editMode, definitionMap);
				String requiredElements[] = at.getElements().entrySet().stream().map(entry -> entry.getValue())
						.filter(formControl -> formControl.isMandatory()).map(formControl -> formControl.getName())
						.toArray(String[]::new);
				if (requiredElements != null && requiredElements.length > 0) {
					ArrayNode reqiuriedNode = this.objectMapper.createArrayNode();
					for (String name : requiredElements) {
						reqiuriedNode.add(name);
					}
					elementPropertiesNode.set("required", reqiuriedNode);
				}
			}

			schemaNode.set("properties", properties);
			if (definitionMap.size() > 0) {
				schemaNode.set("definitions", definitions);
			}

			ArrayNode layoutArray = this.toFormLayoutNode(at);
			if (layoutArray != null && layoutArray.size() > 0) {
				jsonNode.set("layout", layoutArray);
			}
			jsonForm.setFormSchema(jsonNode);
			return jsonForm;
		} catch (RepositoryException e) {
			throw new WcmRepositoryException(e, new WcmError(e.getMessage(), WcmErrors.AT_FORM_ERROR, new String[] {at.getName()}));
		}
	}

	protected void popluateFormControls(HttpServletRequest request, String repository, String workspace,
			ObjectNode propertiesNode, ObjectNode definitions, Map<String, FormControl> formControls, boolean editMode,
			Map<String, String> definitionMap) throws RepositoryException {

		for (String key : formControls.keySet()) {
			FormControl formControl = formControls.get(key);
			if (WcmConstants.CONTROL_TYPE_OBJECT.equals(formControl.getControlType())) {
				this.toTypeDefinition(request, formControl, repository, workspace, formControl.getJcrDataType(),
						definitions, editMode, definitionMap);
				ObjectNode objectNode = this.toAssociationPropertyNode(request, repository, workspace, definitions,
						editMode, definitionMap, formControl);
				propertiesNode.set(key, objectNode);
			} else {
				ObjectNode objectNode = this.toSimplePropertyNode(request, repository, workspace, definitionMap,
						formControl, definitions, editMode);
				propertiesNode.set(key, objectNode);
			}
		}
	}

	protected ObjectNode toJsonFormField(HttpServletRequest request, String repository, String workspace,
			ObjectNode definitions, boolean editMode, Map<String, String> definitionMap, RestPropertyType restProperty,
			FormControl formControl) throws RepositoryException {

		ObjectNode objectNode = this.objectMapper.createObjectNode();
		ObjectNode itemNode = null;
		if (restProperty.isMultiple()) {
			objectNode.put("type", "array");
			if (formControl != null && formControl.getArrayConstraint() != null) {
				this.handleArrayConstraint(request, repository, workspace, definitions, editMode, definitionMap,
						formControl, objectNode);
				formControl = this.getArrayItemFormControl(formControl);
			}
			String schemaType = this.schemaType(restProperty.getRequiredType());
			itemNode = this.objectMapper.createObjectNode();
			itemNode.put("type", schemaType);
			objectNode.set("items", itemNode);

		} else {
			objectNode.put("type", this.schemaType(restProperty.getRequiredType()));
			itemNode = objectNode;
		}
		this.handleSimpleFieldConstraint(request, repository, workspace, definitions, editMode, definitionMap,
				formControl, itemNode);
		return objectNode;
	}

	static Map<String, String> schemaTypeMap = new HashMap<>();
	static {
		schemaTypeMap.put("STRING", "string"); // VARCHAR
		schemaTypeMap.put("BOOLEAN", "boolean"); // BIT
		schemaTypeMap.put("LONG", "integer"); // BIGINT
		schemaTypeMap.put("DOUBLE", "number"); // DOUBLE
		schemaTypeMap.put("DATE", "string"); // DATE
		schemaTypeMap.put("BINARY", "string"); // BINARY

		schemaTypeMap.put("NAME", "string"); // VARCHAR
		schemaTypeMap.put("PATH", "string"); // //VARCHAR
		schemaTypeMap.put("REFERENCE", "string"); //// CHAR(36)
	}

	protected String schemaType(String jcrType) {
		String schemaType = "string";
		if (StringUtils.hasText(jcrType)) {
			schemaType = schemaTypeMap.get(jcrType.toUpperCase());
		}
		return schemaType = (schemaType == null) ? "string" : schemaType;
	}

	protected ArrayNode toFormLayoutNode(Form form) {
		ArrayNode layoutNode = null;
		if (form.getFormLayout() != null && form.getFormLayout().length > 0) {
			layoutNode = this.objectMapper.createArrayNode();
			this.populateFormLayout(form, form.getFormLayout(), layoutNode);
		}
		return layoutNode;
	}
	
	protected ArrayNode toFormLayoutNode(AuthoringTemplate at) {
		ArrayNode layoutNode = null;
		if (at.getElementGroups() != null && at.getElementGroups().length > 0) {
			layoutNode = this.objectMapper.createArrayNode();
			this.populateFormGroups(at, at.getElementGroups(), layoutNode);
		}
		return layoutNode;
	}
	
	protected void populateFormLayout(Form form, BaseFormGroup[] formGroups, ArrayNode layoutNode) {
		for (BaseFormGroup formGroup : formGroups) {
			if (formGroup instanceof FormRow) {
				ObjectNode rowNode = null;
				if (formGroup.isArray()) {
					rowNode = this.getRowArray(form, (FormRow) formGroup);
				} else {
					rowNode = this.getRowNode(form, (FormRow) formGroup);
				}
				layoutNode.add(rowNode);
			}

			if (formGroup instanceof FormRows) {

				for (FormRow formRow : ((FormRows) formGroup).getRows()) {
					ObjectNode rowNode = null;
					if (formGroup.isArray()) {
						rowNode = this.getRowArray(form, formRow);
					} else {
						rowNode = this.getRowNode(form, formRow);
					}
					layoutNode.add(rowNode);
				}
			}

			if (formGroup instanceof FormTabs) {
				if (formGroup.isArray()) {
					this.populateTabArray((FormTabs) formGroup, form, layoutNode);
				} else {
					this.populateTabs((FormTabs) formGroup, form, layoutNode);
				}
			}

			if (formGroup instanceof FormSteps) {
				this.populateSteps((FormSteps) formGroup, form, layoutNode);
			}
		}
	}

	protected void populateFormGroups(AuthoringTemplate at, BaseFormGroup[] formGroups, ArrayNode layoutNode) {
		for (BaseFormGroup formGroup : formGroups) {
			if (formGroup instanceof FormRow) {
				ObjectNode rowNode = null;
				if (formGroup.isArray()) {
					rowNode = this.getRowArray(at, (FormRow) formGroup);
				} else {
					rowNode = this.getRowNode(at, (FormRow) formGroup);
				}
				layoutNode.add(rowNode);
			}

			if (formGroup instanceof FormRows) {

				for (FormRow formRow : ((FormRows) formGroup).getRows()) {
					ObjectNode rowNode = null;
					if (formGroup.isArray()) {
						rowNode = this.getRowArray(at, formRow);
					} else {
						rowNode = this.getRowNode(at, formRow);
					}
					layoutNode.add(rowNode);
				}
			}

			if (formGroup instanceof FormTabs) {
				if (formGroup.isArray()) {
					this.populateTabArray((FormTabs) formGroup, at, layoutNode);
				} else {
					this.populateTabs((FormTabs) formGroup, at, layoutNode);
				}
			}

			if (formGroup instanceof FormSteps) {
				this.populateSteps((FormSteps) formGroup, at, layoutNode);
			}
		}
	}
	
	protected void populateTabArray(FormTabs formTabs, Form form, ArrayNode layoutNode) {
		ObjectNode tabArrayNode = this.objectMapper.createObjectNode();
		tabArrayNode.put("type", "tabarray");
		if (StringUtils.hasText(formTabs.getFiledPath())) {
			tabArrayNode.put("key", formTabs.getFiledPath());
		}
		if (formTabs.getCondition() != null) {
			this.setVisibleCondition(tabArrayNode, formTabs.getCondition());
		}

		ObjectNode arraySectionNode = this.objectMapper.createObjectNode();
		tabArrayNode.set("items", arraySectionNode);
		arraySectionNode.put("type", "section");

		FormTab formTab = formTabs.getTabs()[0];
		ArrayNode arrayItemNodes = this.objectMapper.createArrayNode();
		for (BaseFormGroup formRow : formTab.getFormGroups()) {
			ObjectNode rowNode = this.getRowNode(form, (FormRow) formRow);
			arrayItemNodes.add(rowNode);
		}
		arraySectionNode.set("items", arrayItemNodes);
		layoutNode.add(tabArrayNode);
	}

	protected void populateTabs(FormTabs formTabs, Form form, ArrayNode layoutNode) {
		ObjectNode tabsNode = this.objectMapper.createObjectNode();
		tabsNode.put("type", "tabs");
		if (formTabs.getCondition() != null) {
			this.setVisibleCondition(tabsNode, formTabs.getCondition());
		}
		ArrayNode tabArrayNode = this.objectMapper.createArrayNode();
		for (FormTab formTab : formTabs.getTabs()) {
			ObjectNode tabNode = this.objectMapper.createObjectNode();
			tabNode.put("title", formTab.getTabTitle());
			tabNode.put("type", "tab");
			if (formTab.getCondition() != null) {
				this.setVisibleCondition(tabNode, formTab.getCondition());
			}
			ArrayNode tabItemNodes = this.objectMapper.createArrayNode();
			for (BaseFormGroup formRow : formTab.getFormGroups()) {
				ObjectNode rowNode = this.getRowNode(form, (FormRow) formRow);
				tabItemNodes.add(rowNode);
			}
			tabNode.set("items", tabItemNodes);
			tabArrayNode.add(tabNode);
		}
		tabsNode.set("items", tabArrayNode);
		layoutNode.add(tabsNode);
	}

	protected void populateTabArray(FormTabs formTabs, AuthoringTemplate at, ArrayNode layoutNode) {
		ObjectNode tabArrayNode = this.objectMapper.createObjectNode();
		tabArrayNode.put("type", "tabarray");
		if (StringUtils.hasText(formTabs.getFiledPath())) {
			tabArrayNode.put("key", formTabs.getFiledPath());
		}
		if (formTabs.getCondition() != null) {
			this.setVisibleCondition(tabArrayNode, formTabs.getCondition());
		}

		ObjectNode arraySectionNode = this.objectMapper.createObjectNode();
		tabArrayNode.set("items", arraySectionNode);
		arraySectionNode.put("type", "section");

		FormTab formTab = formTabs.getTabs()[0];
		ArrayNode arrayItemNodes = this.objectMapper.createArrayNode();
		for (BaseFormGroup formRow : formTab.getFormGroups()) {
			ObjectNode rowNode = this.getRowNode(at, (FormRow) formRow);
			arrayItemNodes.add(rowNode);
		}
		arraySectionNode.set("items", arrayItemNodes);
		layoutNode.add(tabArrayNode);
	}

	protected void populateTabs(FormTabs formTabs, AuthoringTemplate at, ArrayNode layoutNode) {
		ObjectNode tabsNode = this.objectMapper.createObjectNode();
		tabsNode.put("type", "tabs");
		if (formTabs.getCondition() != null) {
			this.setVisibleCondition(tabsNode, formTabs.getCondition());
		}
		ArrayNode tabArrayNode = this.objectMapper.createArrayNode();
		for (FormTab formTab : formTabs.getTabs()) {
			ObjectNode tabNode = this.objectMapper.createObjectNode();
			tabNode.put("title", formTab.getTabTitle());
			tabNode.put("type", "tab");
			if (formTab.getCondition() != null) {
				this.setVisibleCondition(tabNode, formTab.getCondition());
			}
			ArrayNode tabItemNodes = this.objectMapper.createArrayNode();
			for (BaseFormGroup formRow : formTab.getFormGroups()) {
				ObjectNode rowNode = this.getRowNode(at, (FormRow) formRow);
				tabItemNodes.add(rowNode);
			}
			tabNode.set("items", tabItemNodes);
			tabArrayNode.add(tabNode);
		}
		tabsNode.set("items", tabArrayNode);
		layoutNode.add(tabsNode);
	}

	protected void setVisibleCondition(ObjectNode widgetNode, VisbleCondition condition) {
		if (StringUtils.hasText(condition.getCondition())) {
			widgetNode.put("condition", condition.getCondition());
		} else if (StringUtils.hasText(condition.getFunctionBody())) {
			ObjectNode functionNode = JsonUtils.createObjectNode();
			widgetNode.set("condition", functionNode);
			functionNode.put("functionBody", condition.getFunctionBody());
		}
	}

	protected void populateSteps(FormSteps formSteps, Form form, ArrayNode layoutNode) {
		ObjectNode stepsNode = this.objectMapper.createObjectNode();
		stepsNode.put("type", "stepper");
		if (formSteps.getCondition() != null) {
			this.setVisibleCondition(stepsNode, formSteps.getCondition());
		}
		ArrayNode stepArrayNode = this.objectMapper.createArrayNode();
		for (FormStep formStep : formSteps.getSteps()) {
			ObjectNode stepNode = this.objectMapper.createObjectNode();
			stepNode.put("title", formStep.getStepTitle());
			stepNode.put("type", "step");
			if (formStep.getCondition() != null) {
				this.setVisibleCondition(stepNode, formStep.getCondition());
			}
			ArrayNode stepItemNodes = this.objectMapper.createArrayNode();
			for (BaseFormGroup formRow : formStep.getFormGroups()) {
				ObjectNode rowNode = this.getRowNode(form, (FormRow) formRow);
				stepItemNodes.add(rowNode);
			}
			stepNode.set("items", stepItemNodes);
			stepArrayNode.add(stepNode);
		}
		stepsNode.set("items", stepArrayNode);
		layoutNode.add(stepsNode);
	}
	
	protected void populateSteps(FormSteps formSteps, AuthoringTemplate at, ArrayNode layoutNode) {
		ObjectNode stepsNode = this.objectMapper.createObjectNode();
		stepsNode.put("type", "stepper");
		if (formSteps.getCondition() != null) {
			this.setVisibleCondition(stepsNode, formSteps.getCondition());
		}
		ArrayNode stepArrayNode = this.objectMapper.createArrayNode();
		for (FormStep formStep : formSteps.getSteps()) {
			ObjectNode stepNode = this.objectMapper.createObjectNode();
			stepNode.put("title", formStep.getStepTitle());
			stepNode.put("type", "step");
			if (formStep.getCondition() != null) {
				this.setVisibleCondition(stepNode, formStep.getCondition());
			}
			ArrayNode stepItemNodes = this.objectMapper.createArrayNode();
			for (BaseFormGroup formRow : formStep.getFormGroups()) {
				ObjectNode rowNode = this.getRowNode(at, (FormRow) formRow);
				stepItemNodes.add(rowNode);
			}
			stepNode.set("items", stepItemNodes);
			stepArrayNode.add(stepNode);
		}
		stepsNode.set("items", stepArrayNode);
		layoutNode.add(stepsNode);
	}

	protected void toTypeDefinition(HttpServletRequest request, FormControl formControl, String repository,
			String workspace, String jcrDataType, ObjectNode definitions, boolean editMode,
			Map<String, String> definitionMap) throws RepositoryException {
		if (formControl != null) {
			if (definitionMap.get(jcrDataType) == null) {
				this.createTypeDefinition(request, formControl, repository, workspace, jcrDataType, definitions, editMode,
						definitionMap);
			}
		}
	}

	protected void createTypeDefinition(HttpServletRequest request, FormControl formControl, String repository,
			String workspace, String jcrDataType, ObjectNode definitions, boolean editMode,
			Map<String, String> definitionMap) throws RepositoryException {

		String baseUrl = RestHelper.repositoryUrl(request);
		RestNodeType restNodeType = nodeTypeHandler.getNodeType(baseUrl, repository, workspace, jcrDataType);
		Set<String> superJcrDataTypes = restNodeType.getSuperTypes();
		boolean hasBpwSuperTypes = false;
		if (superJcrDataTypes != null && superJcrDataTypes.size() > 0) {
			for (String superJcrDataType : superJcrDataTypes) {
				if (superJcrDataType.startsWith(WcmConstants.WCM_NODE_TYPE_PREFIX)) {
					hasBpwSuperTypes = true;
					FormControl superTypeControl = (formControl.getFormControls() == null) ? null
							: formControl.getFormControls().get(superJcrDataType);
					this.toTypeDefinition(request, superTypeControl, repository, workspace, superJcrDataType,
							definitions, editMode, definitionMap);
				}
			}
		}

		ObjectNode definition = this.objectMapper.createObjectNode();
		definition.put("type", "object");
		if (formControl != null && formControl.getObjectConstraint() != null) {

			this.handleObjectConstraint(request, repository, workspace, definitions, editMode, definitionMap,
					formControl, definition);
		}
		ObjectNode properties = this.objectMapper.createObjectNode();
		definition.set("properties", properties);
		for (RestPropertyType restProperty : restNodeType.getPropertyTypes()) {
			String fieldName = this.fieldNameFromNodeTypeName(restProperty.getName());
			ObjectNode fieldNode = this.toJsonFormField(request, repository, workspace, definitions, editMode,
					definitionMap, restProperty, this.getFormControl(formControl, fieldName));
			properties.set(fieldName, fieldNode);
		}
		List<RestChildType> childTypes = restNodeType.getChildTypes();
		if (childTypes != null && childTypes.size() > 0) {
			for (RestChildType restChildType : childTypes) {
				String childFieldType = this.fieldNameFromNodeTypeName(restChildType.getRequiredPrimaryTypeNames()[0]);
				this.toTypeDefinition(request, this.getFormControl(formControl, childFieldType), repository, workspace,
						restChildType.getRequiredPrimaryTypeNames()[0], definitions, editMode, definitionMap);
				ObjectNode refNode = this.objectMapper.createObjectNode();
				ObjectNode itemsNode;
				if ("*".equals(restChildType.getName())) {
					refNode.put("type", "array");
					itemsNode = this.objectMapper.createObjectNode();
					// definition.set("items", itemsNode);
					refNode.set("items", itemsNode);
				} else {
					itemsNode = refNode;
				}
				itemsNode.put(WcmConstants.JSON_SCHEMA_REF, definitionMap.get(childFieldType));
				properties.set(childFieldType, refNode);
			}
		}
		ObjectNode definitionNode = null;
		if (hasBpwSuperTypes) {
			definitionNode = this.objectMapper.createObjectNode();
			ArrayNode allOfArray = this.objectMapper.createArrayNode();
			definitionNode.set(WcmConstants.JSON_SCHEMA_ALL_OF, allOfArray);

			for (String superJcrDataType : superJcrDataTypes) {
				if (superJcrDataType.startsWith(WcmConstants.WCM_NODE_TYPE_PREFIX)) {
					ObjectNode refNode = this.objectMapper.createObjectNode();
					refNode.put(WcmConstants.JSON_SCHEMA_REF, definitionMap.get(superJcrDataType));
					allOfArray.add(refNode);
				}
			}
			allOfArray.add(definition);
		} else {
			definitionNode = definition;
		}
		String fieldType = this.fieldNameFromNodeTypeName(jcrDataType);
		definitions.set(fieldType, definitionNode);
		definitionMap.put(fieldType, WcmUtils.definitionUrl(fieldType));

	}

	protected FormControl getFormControl(FormControl formControl, String propertyName) {
		FormControl result = null;
		if (formControl != null && formControl.getFormControls() != null) {
			result = formControl.getFormControls().get(propertyName);
		}
		return result;
	}

	protected FormControl getArrayItemFormControl(FormControl formControl) {
		FormControl result = null;
		if (formControl != null && formControl.getFormControls() != null) {
			result = formControl.getFormControls().get("items[]");
		}
		return result;
	}

	protected String fieldNameFromNodeTypeName(String typeName) {
		String result = typeName.startsWith("bpw:") ? typeName.substring("bpw:".length()) : typeName;
		return StringUtils.uncapitalize(result);
	}

	protected ObjectNode getRowArray(Form form, FormRow formRow) {
		ObjectNode arrayNode = this.objectMapper.createObjectNode();
		ArrayNode arrayItemNode = this.objectMapper.createArrayNode();
		arrayNode.put("type", "array");
		if (StringUtils.hasText(formRow.getFiledPath())) {
			arrayNode.put("key", formRow.getFiledPath());
		}
		if (formRow.getCondition() != null) {
			this.setVisibleCondition(arrayNode, formRow.getCondition());
		}
		arrayNode.set("items", arrayItemNode);
		ObjectNode rowNode = this.objectMapper.createObjectNode();
		arrayItemNode.add(rowNode);
		rowNode.put("type", "div");
		rowNode.put("flex-flow", "row wrap");
		ArrayNode columnNodes = this.objectMapper.createArrayNode();
		for (FormColumn column : formRow.getColumns()) {
			ObjectNode columnNode = this.getColumnNode(form, column);
			columnNodes.add(columnNode);
		}
		rowNode.set("items", columnNodes);
		return arrayNode;
	}
	
	protected ObjectNode getRowNode(Form form, FormRow formRow) {
		ObjectNode rowNode = this.objectMapper.createObjectNode();
		if (formRow.getCondition() != null) {
			this.setVisibleCondition(rowNode, formRow.getCondition());
		}
		rowNode.put("type", "div");
		rowNode.put("flex-flow", "row wrap");
		ArrayNode columnNodes = this.objectMapper.createArrayNode();
		for (FormColumn column : formRow.getColumns()) {
			ObjectNode columnNode = null;
			if (column.isArray()) {
				columnNode = this.getColumnArray(form, column);
			} else {
				columnNode = this.getColumnNode(form, column);
				columnNodes.add(columnNode);
			}
		}
		rowNode.set("items", columnNodes);
		return rowNode;
	}
	
	protected ObjectNode getRowArray(AuthoringTemplate at, FormRow formRow) {
		ObjectNode arrayNode = this.objectMapper.createObjectNode();
		ArrayNode arrayItemNode = this.objectMapper.createArrayNode();
		arrayNode.put("type", "array");
		if (StringUtils.hasText(formRow.getFiledPath())) {
			arrayNode.put("key", formRow.getFiledPath());

		}
		if (formRow.getCondition() != null) {
			this.setVisibleCondition(arrayNode, formRow.getCondition());
		}
		arrayNode.set("items", arrayItemNode);
		ObjectNode rowNode = this.objectMapper.createObjectNode();
		arrayItemNode.add(rowNode);
		rowNode.put("type", "div");
		rowNode.put("flex-flow", "row wrap");
		ArrayNode columnNodes = this.objectMapper.createArrayNode();
		for (FormColumn column : formRow.getColumns()) {
			ObjectNode columnNode = this.getColumnNode(at, column);
			columnNodes.add(columnNode);
		}
		rowNode.set("items", columnNodes);
		return arrayNode;
	}
	
	protected ObjectNode getRowNode(AuthoringTemplate at, FormRow formRow) {
		ObjectNode rowNode = this.objectMapper.createObjectNode();
		if (formRow.getCondition() != null) {
			this.setVisibleCondition(rowNode, formRow.getCondition());
		}
		rowNode.put("type", "div");
		rowNode.put("flex-flow", "row wrap");
		ArrayNode columnNodes = this.objectMapper.createArrayNode();
		for (FormColumn column : formRow.getColumns()) {
			ObjectNode columnNode = null;
			if (column.isArray()) {
				columnNode = this.getColumnArray(at, column);
			} else {
				columnNode = this.getColumnNode(at, column);
				columnNodes.add(columnNode);
			}
		}
		rowNode.set("items", columnNodes);
		return rowNode;
	}

	protected ObjectNode toAssociationPropertyNode(HttpServletRequest request, String repository, String workspace,
			ObjectNode definitions, boolean editMode, Map<String, String> definitionMap, FormControl formControl)
			throws RepositoryException {
		ObjectNode propertyNode = this.objectMapper.createObjectNode();		
		ObjectNode itemsNode;

		if (formControl.isMultiple()) {
			propertyNode.put("type", "array");
			itemsNode = this.objectMapper.createObjectNode();
			itemsNode.put("type", "object");
			propertyNode.set("items", itemsNode);
			if (formControl.getArrayConstraint() != null) {
				this.handleArrayConstraint(request, repository, workspace, definitions, editMode, definitionMap,
						formControl, propertyNode);
			}
		} else {
			itemsNode = propertyNode;
			itemsNode.put("type", "object");
		}
		
		if (formControl.isUseReference()) {
			itemsNode.put("$ref", WcmUtils.definitionUrl(this.fieldNameFromNodeTypeName(formControl.getJcrDataType())));
		} else {
			ObjectNode propertiesNode = this.objectMapper.createObjectNode();	
			this.popluateFormControls(request, repository, workspace, propertiesNode, definitions, 
					formControl.getFormControls(), editMode, definitionMap);
			itemsNode.set(WcmConstants.JCR_JSON_NODE_PROPERTIES, propertiesNode);
		}
		
		return propertyNode;
	}

	protected ObjectNode toSimplePropertyNode(HttpServletRequest request, String repository, String workspace,
			Map<String, String> definitionMap, FormControl formControl, ObjectNode definitions, boolean editMode)
			throws RepositoryException {
		ObjectNode propertyNode = JsonNodeFactory.instance.objectNode();
		if (formControl.getFormControlLayout() != null
				&& StringUtils.hasText(formControl.getFormControlLayout().getHint())) {
			propertyNode.put("description", formControl.getFormControlLayout().getHint());
		}

		if (formControl.getFormControlLayout() != null && editMode
				&& !formControl.getFormControlLayout().isEditable()) {
			propertyNode.put("readonly", Boolean.TRUE);
		}

		ObjectNode itemsNode = null;
		// if (formControl.isMultiple() && (!"file".equals(formControl.getControlType()))) {
		if (formControl.isMultiple()) {
			propertyNode.put("type", "array");
			itemsNode = JsonNodeFactory.instance.objectNode();
			propertyNode.set("items", itemsNode);
			if (editMode && !formControl.getFormControlLayout().isEditable()) {
				itemsNode.put("readonly", Boolean.TRUE);
			}
			if (formControl.getArrayConstraint() != null) {
				this.handleArrayConstraint(request, repository, workspace, definitions, editMode, definitionMap,
						formControl, propertyNode);
			}

		} else {
			itemsNode = propertyNode;
		}
		
		itemsNode.put("type", formControl.getDataType());

		if (formControl.getFormControlLayout() != null
				&& StringUtils.hasText(formControl.getFormControlLayout().getTitle())) {
			itemsNode.put("title", formControl.getFormControlLayout().getTitle());
		}
		
		this.handleSimpleFieldConstraint(request, repository, workspace, definitions, editMode, definitionMap,
				formControl, itemsNode);

		return propertyNode;
	}

	private void handleSimpleFieldConstraint(HttpServletRequest request, String repository, String workspace,
			ObjectNode definitions, boolean editMode, Map<String, String> definitionMap, FormControl formControl,
			ObjectNode propertyNode) throws RepositoryException {
		if ("String".equalsIgnoreCase(formControl.getDataType()) && formControl.getStringConstraint() != null) {
			this.handleStringConstraint(formControl, propertyNode);
		}

		if ((("integer".equalsIgnoreCase(formControl.getDataType()))
				|| "number".equalsIgnoreCase(formControl.getDataType())) && formControl.getNumberConstraint() != null) {
			this.handleNumberConstraint(formControl, propertyNode);
		}
		this.handleCommonConstraint(formControl, propertyNode);
	}

	private void handleObjectConstraint(HttpServletRequest request, String repository, String workspace,
			ObjectNode definitions, boolean editMode, Map<String, String> definitionMap, FormControl formControl,
			ObjectNode propertyNode) throws RepositoryException {
		ObjectConstraint objectConstraint = formControl.getObjectConstraint();

		if (objectConstraint.getRequired() != null) {
			propertyNode.set("required", WcmUtils.toArrayNode(objectConstraint.getRequired()));
		}

		if (objectConstraint.getMinProperties() != null) {
			propertyNode.put("minProperties", objectConstraint.getMinProperties());
		}

		if (objectConstraint.getMaxProperties() != null) {
			propertyNode.put("maxProperties", objectConstraint.getMaxProperties());
		}
		if (objectConstraint.getAllowAdditionalProperties() != null) {
			propertyNode.put("additionalProperties", "true");
		}

		if (StringUtils.hasText(objectConstraint.getPropertyNamePattern())) {
			ObjectNode patternNode = this.objectMapper.createObjectNode();
			propertyNode.set("propertyNames", patternNode);
			patternNode.put("pattern", WcmUtils.jsonSchemaPattern(objectConstraint.getPropertyNamePattern()));
			
		}

		ObjectNode dependenciesNode = null;
		if (objectConstraint.getDependencies() != null) {
			dependenciesNode = this.objectMapper.createObjectNode();
			propertyNode.set("dependencies", dependenciesNode);
			for (String dependency : objectConstraint.getDependencies().keySet()) {
				dependenciesNode.set(dependency,
						WcmUtils.toArrayNode(objectConstraint.getDependencies().get(dependency)));
			}
		}

		if (objectConstraint.getAdditionalProperties() != null
				&& objectConstraint.getAdditionalProperties().size() > 0) {
			ObjectNode additionalProperties = this.objectMapper.createObjectNode();
			propertyNode.set("additionalProperties", additionalProperties);
			this.popluateFormControls(request, repository, workspace, additionalProperties, definitions,
					objectConstraint.getAdditionalProperties(), editMode, definitionMap);
		}
		if (objectConstraint.getPatternProperties() != null && objectConstraint.getPatternProperties().size() > 0) {
			ObjectNode patternProperties = this.objectMapper.createObjectNode();
			propertyNode.set("patternProperties", patternProperties);
			this.popluateFormControls(request, repository, workspace, patternProperties, definitions,
					objectConstraint.getPatternProperties(), editMode, definitionMap);
		}

		if (objectConstraint.getSchemaDependencies() != null && objectConstraint.getSchemaDependencies().size() > 0) {
			if (dependenciesNode == null) {
				dependenciesNode = this.objectMapper.createObjectNode();
				propertyNode.set("dependencies", dependenciesNode);
			}
			this.popluateFormControls(request, repository, workspace, dependenciesNode, definitions,
					objectConstraint.getSchemaDependencies(), editMode, definitionMap);
		}
	}

	private void handleArrayConstraint(HttpServletRequest request, String repository, String workspace,
			ObjectNode definitions, boolean editMode, Map<String, String> definitionMap, FormControl formControl,
			ObjectNode propertyNode) throws RepositoryException {
		ArrayConstraint arrayConstraint = formControl.getArrayConstraint();
		if (arrayConstraint.getMaxItems() != null) {
			propertyNode.put("maxItems", arrayConstraint.getMaxItems());
		}
		if (arrayConstraint.getMinItems() != null) {
			propertyNode.put("minItems", arrayConstraint.getMinItems());
		}
		if (arrayConstraint.getUniqueItems() != null) {
			propertyNode.put("uniqueItems", arrayConstraint.getUniqueItems());
		}

		if (arrayConstraint.getContains() != null) {
			ObjectNode containNode = JsonNodeFactory.instance.objectNode();

			propertyNode.set("contains", containNode);
			containNode.put("type", arrayConstraint.getContains());
		}
		if (arrayConstraint.getAdditionalItems() != null && arrayConstraint.getAdditionalItems().size() > 0) {

			ObjectNode additionalItems = this.objectMapper.createObjectNode();
			propertyNode.set("additionalItems", additionalItems);
			this.popluateFormControls(request, repository, workspace, additionalItems, definitions,
					arrayConstraint.getAdditionalItems(), editMode, definitionMap);
		}
	}

	private void handleCommonConstraint(FormControl formControl, ObjectNode propertyNode) {
		if (formControl.getConstraint() != null) {
			CommonConstraint commonConstraint = formControl.getConstraint();
			if (commonConstraint.getEnumeration() != null && formControl.getConstraint().getEnumeration().length > 0) {
				propertyNode.set("enum", WcmUtils.toArrayNode(commonConstraint.getEnumeration()));
			}

			if (commonConstraint.getDefaultValues() != null
					&& formControl.getConstraint().getDefaultValues().length > 0) {
				propertyNode.set(WcmConstants.DEFAULT_WS, WcmUtils.toArrayNode(commonConstraint.getDefaultValues()));
			}

			if (StringUtils.hasText(commonConstraint.getConstant())) {
				propertyNode.put("const", commonConstraint.getConstant());
			}
		}
	}

	private void handleStringConstraint(FormControl formControl, ObjectNode propertyNode) {
		if (formControl.getStringConstraint() != null) {
			StringConstraint stringConstraint = formControl.getStringConstraint();

			if (stringConstraint.getMinLength() != null) {
				propertyNode.put("minLength", stringConstraint.getMinLength());
			}

			if (stringConstraint.getMaxLength() != null) {
				propertyNode.put("maxLength", stringConstraint.getMaxLength());
			}

			if (StringUtils.hasText(stringConstraint.getFormat())) {
				propertyNode.put("format", stringConstraint.getFormat());
			}

			if (StringUtils.hasText(stringConstraint.getPattern())) {
				propertyNode.put("pattern", WcmUtils.jsonSchemaPattern(stringConstraint.getPattern()));
			}

			if (StringUtils.hasText(stringConstraint.getContentEncoding())) {
				propertyNode.put("contentEncoding", stringConstraint.getContentEncoding());
			}

			if (StringUtils.hasText(stringConstraint.getContentMediaType())) {
				propertyNode.put("contentMediaType", stringConstraint.getContentMediaType());
			}

			if (StringUtils.hasText(stringConstraint.getContentSchema())) {
				propertyNode.put("contentSchema", stringConstraint.getContentSchema());
			}
		}
	}

	private void handleNumberConstraint(FormControl formControl, ObjectNode propertyNode) {
		if (formControl.getStringConstraint() != null) {
			NumberConstraint numberConstraint = formControl.getNumberConstraint();

			if (numberConstraint.getMultipleOf() != null) {
				propertyNode.put("multipleOf", numberConstraint.getMultipleOf());
			}

			if (numberConstraint.getMaximum() != null) {
				if (numberConstraint.getExclusiveMaximum()) {
					propertyNode.put("exclusiveMaximum", numberConstraint.getMinimum());
				} else {
					propertyNode.put("maximum", numberConstraint.getMaximum());
				}
			}

			if (numberConstraint.getMinimum() != null) {
				if (numberConstraint.getExclusiveMinimum()) {
					propertyNode.put("exclusiveMinimum", numberConstraint.getMinimum());
				} else {
					propertyNode.put("minimum", numberConstraint.getMinimum());
				}
			}
		}
	}

	protected ObjectNode getColumnArray(Form form, FormColumn formColumn) {
		ObjectNode arrayNode = this.objectMapper.createObjectNode();
		ArrayNode arrayItemNode = this.objectMapper.createArrayNode();
		arrayNode.put("type", "array");
		if (StringUtils.hasText(formColumn.getFiledPath())) {
			arrayNode.put("key", formColumn.getFiledPath());
		}
		if (formColumn.getCondition() != null) {
			this.setVisibleCondition(arrayNode, formColumn.getCondition());
		}
		arrayNode.set("items", arrayItemNode);
		arrayItemNode.add(this.getColumnNode(form, formColumn));
		return arrayNode;
	}

	protected ObjectNode getColumnNode(Form form, FormColumn formColumn) {
		ObjectNode columnNode = this.objectMapper.createObjectNode();
		columnNode.put("type", "div");
		columnNode.put("displayFlex", true);
		columnNode.put("flex-direction", "column");
		columnNode.put("fxFlex", formColumn.getFxFlex());

		if (formColumn.getCondition() != null) {
			this.setVisibleCondition(columnNode, formColumn.getCondition());
		}

		ArrayNode fieldNodes = this.objectMapper.createArrayNode();
		ArrayNode layoutNodes = fieldNodes;
		boolean addChildNodesByDefault = formColumn.getFormGroups() == null || formColumn.getFormGroups().length == 0;
		for (String fieldPath : formColumn.getFormControls()) {
			FormControl formControl = WcmUtils.getFormControl(form, fieldPath);

			if (formControl == null) {
				throw new WcmRepositoryException(
						new WcmError(
							String.format("FormControl %s can not be found in form template %s", fieldPath, form.getName()),
							WcmErrors.FORM_CONTROL_NOT_FOUND, 
							new String[] {fieldPath, form.getName()})
				);
			}

			layoutNodes = this.buildFieldLayout(fieldPath, formControl, fieldNodes, form, addChildNodesByDefault);
		}

		if (!addChildNodesByDefault) {
			if (formColumn.getFormGroups() != null && formColumn.getFormGroups().length > 0) {
				this.populateFormLayout(form, formColumn.getFormGroups(), layoutNodes);
			}
		}
		columnNode.set("items", fieldNodes);
		return columnNode;
	}
	
	protected ObjectNode getColumnArray(AuthoringTemplate at, FormColumn formColumn) {
		ObjectNode arrayNode = this.objectMapper.createObjectNode();
		ArrayNode arrayItemNode = this.objectMapper.createArrayNode();
		arrayNode.put("type", "array");
		if (StringUtils.hasText(formColumn.getFiledPath())) {
			arrayNode.put("key", formColumn.getFiledPath());
		}
		if (formColumn.getCondition() != null) {
			this.setVisibleCondition(arrayNode, formColumn.getCondition());
		}
		arrayNode.set("items", arrayItemNode);
		arrayItemNode.add(this.getColumnNode(at, formColumn));
		return arrayNode;
	}

	protected ObjectNode getColumnNode(AuthoringTemplate at, FormColumn formColumn) {
		ObjectNode columnNode = this.objectMapper.createObjectNode();
		columnNode.put("type", "div");
		columnNode.put("displayFlex", true);
		columnNode.put("flex-direction", "column");
		columnNode.put("fxFlex", formColumn.getFxFlex());

		if (formColumn.getCondition() != null) {
			this.setVisibleCondition(columnNode, formColumn.getCondition());
		}

		ArrayNode fieldNodes = this.objectMapper.createArrayNode();
		ArrayNode layoutNodes = fieldNodes;
		boolean addChildNodesByDefault = formColumn.getFormGroups() == null || formColumn.getFormGroups().length == 0;
		for (String fieldPath : formColumn.getFormControls()) {
			FormControl formControl = WcmUtils.getFormControl(at, fieldPath);

			if (formControl == null) {
				throw new WcmRepositoryException(
						new WcmError(
							String.format("FormControl %s can not be found in authoring template %s", fieldPath, at.getName()),
							WcmErrors.AT_CONTROL_NOT_FOUND, 
							new String[] {fieldPath, at.getName()})
				);
			}

			layoutNodes = this.buildFieldLayout(fieldPath, formControl, fieldNodes, at, addChildNodesByDefault);
		}

		if (!addChildNodesByDefault) {
			if (formColumn.getFormGroups() != null && formColumn.getFormGroups().length > 0) {
				this.populateFormGroups(at, formColumn.getFormGroups(), layoutNodes);
			}
		}

		columnNode.set("items", fieldNodes);
		return columnNode;
	}

	protected ArrayNode buildFieldLayout(String fieldPath, FormControl formControl, ArrayNode fieldNodes,
			Form form, boolean addChildNodesByDefault) {
		ArrayNode layoutNodes = null;
		if (formControl.getFormControls() == null || formControl.getFormControls().size() == 0) {
			layoutNodes = this.buildLeafNodeLayout(fieldPath, formControl, fieldNodes, form);
		} else {
			layoutNodes = this.buildCompositeNodeLayout(fieldPath, formControl, fieldNodes, form, addChildNodesByDefault);
		}
		return layoutNodes;
	}
	
	protected ArrayNode buildLeafNodeLayout(String fieldPath, FormControl formControl, ArrayNode fieldNodes,
			Form form) {

		ObjectNode fieldNode = this.objectMapper.createObjectNode();

		if (formControl.getFormControlLayout() != null) {
			if (formControl.getFormControlLayout().isExpandable()) {
				ObjectNode sectionNode = this.objectMapper.createObjectNode();
				sectionNode.put("type", "section");
				sectionNode.set("items", fieldNode);
				sectionNode.put("expandable", true);
				sectionNode.put("expanded", formControl.getFormControlLayout().isExpanded());
				fieldNodes.add(sectionNode);
			} else {
				fieldNodes.add(fieldNode);
			}
			if (StringUtils.hasText(formControl.getFormControlLayout().getTitle())) {
				fieldNode.put("title", formControl.getFormControlLayout().getTitle());
			} else {
				fieldNode.put("notitle", "true");
			}

			if (StringUtils.hasText(formControl.getFormControlLayout().getFlex())) {
				fieldNode.put("flex", formControl.getFormControlLayout().getFlex());
			}
			if (StringUtils.hasText(formControl.getFormControlLayout().getPlaceHolder())) {
				fieldNode.put("placeHolder", formControl.getFormControlLayout().getPlaceHolder());
			}
		} else {
			fieldNodes.add(fieldNode);
		}

		if (formControl.isMultiple() && (!"file".equals(formControl.getControlType()))) {
			fieldNode.put("type", "array");
			if (formControl.getFormControlLayout() != null && formControl.getFormControlLayout().getListItems() > 0) {
				fieldNode.put("listItems", formControl.getFormControlLayout().getListItems());
			}
			ObjectNode itemsNode = this.objectMapper.createObjectNode();
			fieldNode.set("items", itemsNode);
			itemsNode.put("key", WcmUtils.layoutPath(fieldPath, form));
			fieldNode = itemsNode;
		} else {
			fieldNode.put("key", WcmUtils.layoutPath(fieldPath, form));
		}
		
		if ("file".equals(formControl.getControlType())) {
			if (formControl.isMultiple()) {
				fieldNode.put("multiple", "true");
			}
			fieldNode.put("format", formControl.getFormat());
		}

		if (StringUtils.hasText(formControl.getControlType())) {
			fieldNode.put("type", formControl.getControlType());
			if (formControl.getFormControlLayout() != null) {
				if ("textarea".equals(formControl.getControlType())) {
					if (formControl.getFormControlLayout().getCols() != null) {
						fieldNode.put("cols", formControl.getFormControlLayout().getCols());
					}
					if (formControl.getFormControlLayout().getRows() != null) {
						fieldNode.put("rows", formControl.getFormControlLayout().getRows());
					}
				}
			}
		}

		if (formControl.getCustomConstraint() != null) {
			CustomConstraint customConstraint = formControl.getCustomConstraint();
			ObjectNode functionsNode = this.objectMapper.createObjectNode();
			fieldNode.set("functions", functionsNode);
			for (JavascriptFunction function : customConstraint.getJavascriptFunction()) {
				ObjectNode functionNode = this.objectMapper.createObjectNode();
				functionsNode.set(function.getName(), functionNode);
				functionNode.set("arguments", WcmUtils.toArrayNode(function.getParams()));
				functionNode.put("body", function.getFunctionBody());
			}

		}
		return fieldNodes;
	}

	protected ArrayNode buildCompositeNodeLayout(String fieldPath, FormControl formControl, ArrayNode fieldNodes,
			Form form, boolean addChildNodesByDefault) {

		// Array or section
		String names[] = this.getNameAndPrefix(fieldPath);
		String name = names[1];
		// Form section
		ArrayNode layoutNodes = null;
		ObjectNode fieldNode = this.objectMapper.createObjectNode();

		fieldNode.put("title", name);
		if (formControl.getFormControlLayout() != null) {
			if (StringUtils.hasText(formControl.getFormControlLayout().getTitle())) {
				fieldNode.put("title", formControl.getFormControlLayout().getTitle());
			}

		}
		if (formControl.isMultiple()) { // Array Items is of type Object,

			fieldNode.put("type", "array");
			// fieldNode.put("key", WcmUtils.layoutPath(fieldPath, form));
			fieldNode.put("key", fieldPath);
			if (formControl.getFormControlLayout() != null) {
				if (formControl.getFormControlLayout().isExpandable()) {
					ObjectNode sectionNode = this.objectMapper.createObjectNode();
					sectionNode.put("type", "section");
					sectionNode.set("items", fieldNode);
					sectionNode.put("expandable", true);
					sectionNode.put("expanded", formControl.getFormControlLayout().isExpanded());
					fieldNodes.add(sectionNode);
				} else {
					fieldNodes.add(fieldNode);
				}

				if (formControl.getFormControlLayout().getListItems() > 0) {
					fieldNode.put("listItems", formControl.getFormControlLayout().getListItems());
				}

				if (formControl.getFormControlLayout().isDisplayFlex()) {
					ObjectNode itemsNode = this.objectMapper.createObjectNode();
					fieldNode.set("items", itemsNode);
					itemsNode.put("type", "div");
					itemsNode.put("displayFlex", true);
					if (StringUtils.hasText(formControl.getFormControlLayout().getFlex())) {
						itemsNode.put("flex", formControl.getFormControlLayout().getFlex());
					}
					itemsNode.put("flex-direction",
							StringUtils.hasText(formControl.getFormControlLayout().getFlexDirection())
									? formControl.getFormControlLayout().getFlexDirection()
									: "row");
					layoutNodes = this.objectMapper.createArrayNode();
					itemsNode.set("items", layoutNodes);
				} else {
					layoutNodes = this.objectMapper.createArrayNode();
					fieldNode.set("items", layoutNodes);
				}
			} else {
				layoutNodes = this.objectMapper.createArrayNode();
				fieldNode.set("items", layoutNodes);
				fieldNodes.add(fieldNode);
			}
		} else { // section, div etc
			fieldNodes.add(fieldNode);
			fieldNode.put("type", formControl.getControlType());
			//fieldNode.put("key", WcmUtils.layoutPath(fieldPath, form));
			fieldNode.put("key", fieldPath);
			if (formControl.getFormControlLayout() != null) {
				if (formControl.getFormControlLayout().isExpandable()) {
					fieldNode.put("expandable", true);
					fieldNode.put("expanded", formControl.getFormControlLayout().isExpanded());
				}
				if (formControl.getFormControlLayout().isDisplayFlex()) {
					fieldNode.put("displayFlex", true);
					if (StringUtils.hasText(formControl.getFormControlLayout().getFlex())) {
						fieldNode.put("flex", formControl.getFormControlLayout().getFlex());
					}
					fieldNode.put("flex-direction",
							StringUtils.hasText(formControl.getFormControlLayout().getFlexDirection())
									? formControl.getFormControlLayout().getFlexDirection()
									: "row");
				}
			}
			layoutNodes = this.objectMapper.createArrayNode();
			fieldNode.set("items", layoutNodes);
		}
		if (addChildNodesByDefault) {
			for (FormControl childFormControl : formControl.getFormControls().values()) {
				String childFieldPath = WcmUtils.fieldPath(fieldPath, childFormControl.getName());
				this.buildFieldLayout(childFieldPath, childFormControl, layoutNodes, form, true);
			}
		}
		return layoutNodes;
		
	}
	
	protected ArrayNode buildFieldLayout(String fieldPath, FormControl formControl, ArrayNode fieldNodes,
			AuthoringTemplate at, boolean addChildNodesByDefault) {
		ArrayNode layoutNodes = null;
		if (formControl.getFormControls() == null || formControl.getFormControls().size() == 0) {
			layoutNodes = this.buildLeafNodeLayout(fieldPath, formControl, fieldNodes, at);
		} else {
			layoutNodes = this.buildCompositeNodeLayout(fieldPath, formControl, fieldNodes, at, addChildNodesByDefault);
		}
		return layoutNodes;
	}

	protected ArrayNode buildLeafNodeLayout(String fieldPath, FormControl formControl, ArrayNode fieldNodes,
			AuthoringTemplate at) {

		ObjectNode fieldNode = this.objectMapper.createObjectNode();

		if (formControl.getFormControlLayout() != null) {
			if (formControl.getFormControlLayout().isExpandable()) {
				ObjectNode sectionNode = this.objectMapper.createObjectNode();
				sectionNode.put("type", "section");
				sectionNode.set("items", fieldNode);
				sectionNode.put("expandable", true);
				sectionNode.put("expanded", formControl.getFormControlLayout().isExpanded());
				fieldNodes.add(sectionNode);
			} else {
				fieldNodes.add(fieldNode);
			}
			if (StringUtils.hasText(formControl.getFormControlLayout().getTitle())) {
				fieldNode.put("title", formControl.getFormControlLayout().getTitle());
			} else {
				fieldNode.put("notitle", "true");
			}

			if (StringUtils.hasText(formControl.getFormControlLayout().getFlex())) {
				fieldNode.put("flex", formControl.getFormControlLayout().getFlex());
			}
			if (StringUtils.hasText(formControl.getFormControlLayout().getPlaceHolder())) {
				fieldNode.put("placeHolder", formControl.getFormControlLayout().getPlaceHolder());
			}
		} else {
			fieldNodes.add(fieldNode);
		}

		if (formControl.isMultiple() && (!"file".equals(formControl.getControlType()))) {
			fieldNode.put("type", "array");
			if (formControl.getFormControlLayout() != null && formControl.getFormControlLayout().getListItems() > 0) {
				fieldNode.put("listItems", formControl.getFormControlLayout().getListItems());
			}
			ObjectNode itemsNode = this.objectMapper.createObjectNode();
			fieldNode.set("items", itemsNode);
			itemsNode.put("key", WcmUtils.layoutPath(fieldPath, at));
			fieldNode = itemsNode;
		} else {
			fieldNode.put("key", WcmUtils.layoutPath(fieldPath, at));
		}
		if (formControl.isMultiple() && "file".equals(formControl.getControlType())) {
			fieldNode.put("multiple", "true");
		}
		if (StringUtils.hasText(formControl.getControlType())) {
			fieldNode.put("type", formControl.getControlType());
			if (formControl.getFormControlLayout() != null) {
				if ("textarea".equals(formControl.getControlType())) {
					if (formControl.getFormControlLayout().getCols() != null) {
						fieldNode.put("cols", formControl.getFormControlLayout().getCols());
					}
					if (formControl.getFormControlLayout().getRows() != null) {
						fieldNode.put("rows", formControl.getFormControlLayout().getRows());
					}
				}
			}
		}

		if (formControl.getCustomConstraint() != null) {
			CustomConstraint customConstraint = formControl.getCustomConstraint();
			ObjectNode functionsNode = this.objectMapper.createObjectNode();
			fieldNode.set("functions", functionsNode);
			for (JavascriptFunction function : customConstraint.getJavascriptFunction()) {
				ObjectNode functionNode = this.objectMapper.createObjectNode();
				functionsNode.set(function.getName(), functionNode);
				functionNode.set("arguments", WcmUtils.toArrayNode(function.getParams()));
				functionNode.put("body", function.getFunctionBody());
			}

		}
		return fieldNodes;
	}

	protected ArrayNode buildCompositeNodeLayout(String fieldPath, FormControl formControl, ArrayNode fieldNodes,
			AuthoringTemplate at, boolean addChildNodesByDefault) {

		// Array or section
		String names[] = this.getNameAndPrefix(fieldPath);
		String name = names[1];
		// Form section
		ArrayNode layoutNodes = null;
		ObjectNode fieldNode = this.objectMapper.createObjectNode();

		fieldNode.put("title", name);
		if (formControl.getFormControlLayout() != null) {
			if (StringUtils.hasText(formControl.getFormControlLayout().getTitle())) {
				fieldNode.put("title", formControl.getFormControlLayout().getTitle());
			}

		}
		if (formControl.isMultiple()) { // Array Items is of type Object,

			fieldNode.put("type", "array");
			fieldNode.put("key", fieldPath);
			layoutNodes = this.objectMapper.createArrayNode();
			if (formControl.getFormControlLayout() != null) {
				if (formControl.getFormControlLayout().isExpandable()) {
					ObjectNode sectionNode = this.objectMapper.createObjectNode();
					sectionNode.put("type", "section");
					sectionNode.set("items", fieldNode);
					sectionNode.put("expandable", true);
					sectionNode.put("expanded", formControl.getFormControlLayout().isExpanded());
					fieldNodes.add(sectionNode);
				} else {
					fieldNodes.add(fieldNode);
				}

				if (formControl.getFormControlLayout().getListItems() > 0) {
					fieldNode.put("listItems", formControl.getFormControlLayout().getListItems());
				}

				if (formControl.getFormControlLayout().isDisplayFlex()) {
					ObjectNode itemsNode = this.objectMapper.createObjectNode();
					fieldNode.set("items", itemsNode);
					itemsNode.put("type", "div");
					itemsNode.put("displayFlex", true);
					if (StringUtils.hasText(formControl.getFormControlLayout().getFlex())) {
						itemsNode.put("flex", formControl.getFormControlLayout().getFlex());
					}
					itemsNode.put("flex-direction",
							StringUtils.hasText(formControl.getFormControlLayout().getFlexDirection())
									? formControl.getFormControlLayout().getFlexDirection()
									: "row");
					itemsNode.set("items", layoutNodes);
				} else {
					fieldNode.set("items", layoutNodes);
				}
			} else {
				fieldNode.set("items", layoutNodes);
			}
			fieldNodes = layoutNodes;
		} else { // section, div etc
			fieldNodes.add(fieldNode);
			fieldNode.put("type", formControl.getControlType());
			fieldNode.put("key", fieldPath);
			if (formControl.getFormControlLayout() != null) {
				if (formControl.getFormControlLayout().isExpandable()) {
					fieldNode.put("expandable", true);
					fieldNode.put("expanded", formControl.getFormControlLayout().isExpanded());
				}
				if (formControl.getFormControlLayout().isDisplayFlex()) {
					fieldNode.put("displayFlex", true);
					if (StringUtils.hasText(formControl.getFormControlLayout().getFlex())) {
						fieldNode.put("flex", formControl.getFormControlLayout().getFlex());
					}
					fieldNode.put("flex-direction",
							StringUtils.hasText(formControl.getFormControlLayout().getFlexDirection())
									? formControl.getFormControlLayout().getFlexDirection()
									: "row");
				}
			}
			layoutNodes = this.objectMapper.createArrayNode();
			fieldNode.set("items", layoutNodes);
		}
		if (addChildNodesByDefault) {
			for (FormControl childFormControl : formControl.getFormControls().values()) {
				String childFieldPath = WcmUtils.fieldPath(fieldPath, childFormControl.getName());
				this.buildFieldLayout(childFieldPath, childFormControl, layoutNodes, at, true);
			}
		}
		return layoutNodes;
	}

	protected String[] getNameAndPrefix(String fieldName) {
		String names[] = fieldName.split("\\.", 2);
		if (names.length == 1) {
			names = new String[] { "", names[0] };
		}
		return names;
	}

	protected String getJcrType(RestNode node) {
		String jcrType = "";
		for (RestProperty property : node.getJcrProperties()) {
			if ("bpw:jcrType".equals(property.getName())) {
				jcrType = property.getValues().get(0);
				break;
			}
		}
		return jcrType;
	}

	protected Stream<RenderTemplate> getRenderTemplates(RenderTemplate rt, HttpServletRequest request)
			throws WcmRepositoryException {
		String absPath = "/library/" + rt.getLibrary() + "/renderTemplate";
		try {
			String baseUrl = RestHelper.repositoryUrl(request);
			
			RestNode themeNode = (RestNode) this.wcmItemHandler.item(baseUrl, rt.getRepository(), rt.getWorkspace(),
					 absPath, WcmConstants.RENDER_TEMPLATE_DEPATH);
			return themeNode.getChildren().stream().filter(this::isRenderTemplate)
					.map(node -> this.toRenderTemplate(node, rt.getRepository(), rt.getWorkspace(), rt.getLibrary(), request));
		} catch (RepositoryException e) {
			throw new WcmRepositoryException(e, new WcmError(e.getMessage(), WcmErrors.AT_CONTROL_NOT_FOUND, new String[] { absPath} ));
		}
	}

	public RenderTemplate toRenderTemplate(RestNode node, String repository, String workspace, String library, HttpServletRequest request) {
		RenderTemplate rt = new RenderTemplate();
		rt.setWcmAuthority(WcmUtils.getWcmAuthority(null));
		rt.setRepository(repository);
		rt.setWorkspace(workspace);
		rt.setLibrary(library);
		rt.setName(node.getName());
		this.wcmUtils.resolveResourceNode(rt, node);
		for (RestProperty property : node.getJcrProperties()) {
			if ("bpw:code".equals(property.getName())) {
				rt.setCode(property.getValues().get(0));
			} else if ("bpw:preloop".equals(property.getName())) {
				rt.setPreloop(property.getValues().get(0));
			} else if ("bpw:postloop".equals(property.getName())) {
				rt.setPostloop(property.getValues().get(0));
			} else if ("bpw:maxEntries".equals(property.getName())) {
				rt.setMaxEntries(Integer.parseInt(property.getValues().get(0)));
			} else if ("bpw:note".equals(property.getName())) {
				rt.setNote(property.getValues().get(0));
			} else if ("bpw:resourceName".equals(property.getName())) {
				rt.setResourceName(property.getValues().get(0));
			} else if ("bpw:isQuery".equals(property.getName())) {
				rt.setQuery(Boolean.parseBoolean(property.getValues().get(0)));
			}
		}
		
		if (StringUtils.hasText(rt.getResourceName())) {
			AuthoringTemplate at = this.getAuthoringTemplate(repository, workspace, rt.getResourceName(), request);
			rt.setNodeType(at.getNodeType());
		}

		List<RenderTemplateLayoutRow> rows = new ArrayList<>();
		for (RestNode rowNode : node.getChildren()) {
			if (WcmUtils.checkNodeType(rowNode, "bpw:RenderTemplateLayoutRow")) {
				RenderTemplateLayoutRow row = new RenderTemplateLayoutRow();
				rows.add(row);
				List<RenderTemplateLayoutColumn> columns = new ArrayList<>();
				for (RestNode columnNode : rowNode.getChildren()) {
					if (WcmUtils.checkNodeType(columnNode, "bpw:RenderTemplateLayoutColumn")) {
						RenderTemplateLayoutColumn column = new RenderTemplateLayoutColumn();
						columns.add(column);
						for (RestProperty property : columnNode.getJcrProperties()) {
							if ("bpw:id".equals(property.getName())) {
								column.setId(property.getValues().get(0));
							} else if ("bpw:widtd".equals(property.getName())) {
								column.setWidth(Integer.parseInt(property.getValues().get(0)));
							}
						}
						List<ResourceElementRender> elements = new ArrayList<>();
						for (RestNode elementNode : columnNode.getChildren()) {
							if (WcmUtils.checkNodeType(elementNode, "bpw:ResourceElementRender")) {
								ResourceElementRender element = new ResourceElementRender();
								elements.add(element);
								element.setName(elementNode.getName());
								for (RestProperty property : elementNode.getJcrProperties()) {
									if ("bpw:source".equals(property.getName())) {
										element.setSource(property.getValues().get(0));
									} else if ("bpw:body".equals(property.getName())) {
										element.setBody(property.getValues().get(0));
									}
								}
							}
						}
						column.setElements(elements.toArray(new ResourceElementRender[elements.size()]));

					}
				}
				row.setColumns(columns.toArray(new RenderTemplateLayoutColumn[columns.size()]));
			}
		}

		if (rows.size() > 0) {
			rt.setRows(rows.toArray(new RenderTemplateLayoutRow[rows.size()]));
		}
		return rt;
	}

	protected RenderTemplate toRenderTemplateWithLibrary(RestNode node, String repository, String workspace) {
		RenderTemplate rtWithLibrary = new RenderTemplate();
		rtWithLibrary.setRepository(repository);
		rtWithLibrary.setWorkspace(workspace);
		rtWithLibrary.setLibrary(node.getName());
		return rtWithLibrary;
	}

	public Map<String, RenderTemplate> getRenderTemplates(String repository, String workspace,
			HttpServletRequest request) throws WcmRepositoryException {
		Map<String, RenderTemplate> renderTemplates = this.getRenderTemplateLibraries(repository, workspace, request)
				.flatMap(rt -> this.getRenderTemplates(rt, request))
				.collect(Collectors.toMap(
						rt -> String.format(WcmConstants.WCM_RT_PATH_PATTERN, rt.getLibrary(), rt.getName()),
						Function.identity()));

		return renderTemplates;

	}

	protected Stream<RenderTemplate> getRenderTemplateLibraries(String repository, String workspace,
			HttpServletRequest request) throws WcmRepositoryException {
		try {
			String baseUrl = RestHelper.repositoryUrl(request);
			RestNode bpwizardNode = (RestNode) this.wcmItemHandler.item(baseUrl, repository, workspace, "/library",
					WcmConstants.READ_DEPTH_DEFAULT);
			return bpwizardNode.getChildren().stream().filter(this::isLibrary).filter(this::notSystemLibrary)
					.map(node -> toRenderTemplateWithLibrary(node, repository, workspace));
		} catch (RepositoryException e) {
			throw new WcmRepositoryException(e, new WcmError(e.getMessage(), WcmErrors.GET_NODE_ERROR, null));
		}
	}

	public Map<String, ContentAreaLayout> getContentAreaLayouts(String repository, String workspace,
			HttpServletRequest request) throws WcmRepositoryException {
		String baseUrl = RestHelper.repositoryUrl(request);
		Map<String, ContentAreaLayout> contentAreaLayouts = this
				.getContentAreaLayoutLibraries(repository, workspace, baseUrl)
				.flatMap(layout -> this.getContentArealayouts(layout, baseUrl))
				.collect(Collectors.toMap(layout -> String.format(WcmConstants.WCM_CONTENT_LAYOUT_PATH_PATTERN,
						layout.getLibrary(), layout.getName()), Function.identity()));

		return contentAreaLayouts;
	}

	protected Stream<ContentAreaLayout> getContentAreaLayoutLibraries(String repository, String workspace,
			String baseUrl) throws WcmRepositoryException {
		try {
			RestNode bpwizardNode = (RestNode) this.wcmItemHandler.item(baseUrl, repository, workspace, "/library",
					WcmConstants.READ_DEPTH_DEFAULT);
			return bpwizardNode.getChildren().stream().filter(this::isLibrary).filter(this::notSystemLibrary)
					.map(node -> toContentAreaLayoutWithLibrary(node, repository, workspace));
		} catch (RepositoryException e) {
			throw new WcmRepositoryException(e, new WcmError(e.getMessage(), WcmErrors.GET_NODE_ERROR, null));
		}
	}

	private Stream<ContentAreaLayout> getContentArealayouts(ContentAreaLayout contentAreaLayout, String baseUrl)
			throws WcmRepositoryException {
		String absPath = "/library/" + contentAreaLayout.getLibrary() + "/contentAreaLayout";
		try {
			RestNode contentAreaLayoutNode = (RestNode) this.wcmItemHandler.item(baseUrl,
					contentAreaLayout.getRepository(), contentAreaLayout.getWorkspace(),
					absPath,
					WcmConstants.CONTENT_AREA_LAYOUT_DEPTH);
			return contentAreaLayoutNode.getChildren().stream().filter(this::isContentAreaLayout)
					.map(node -> this.toContentAreaLayout(node, contentAreaLayout));
		} catch (RepositoryException e) {
			throw new WcmRepositoryException(e, new WcmError(e.getMessage(), WcmErrors.GET_CONTENT_AREA_LAYOUT_ERROR, new String[] { absPath}));
		}
	}

	private ContentAreaLayout toContentAreaLayoutWithLibrary(RestNode node, String repository, String workspace) {
		ContentAreaLayout contentAreaLayoutWithLibrary = new ContentAreaLayout();
		contentAreaLayoutWithLibrary.setRepository(repository);
		contentAreaLayoutWithLibrary.setWorkspace(workspace);
		contentAreaLayoutWithLibrary.setLibrary(node.getName());
		return contentAreaLayoutWithLibrary;
	}

	public ContentAreaLayout toContentAreaLayout(RestNode node, ContentAreaLayout layout) {
		ContentAreaLayout contentAreaLayout = new ContentAreaLayout();
		contentAreaLayout.setWcmAuthority(WcmUtils.getWcmAuthority(null));
		contentAreaLayout.setRepository(layout.getRepository());
		contentAreaLayout.setWorkspace(layout.getWorkspace());
		contentAreaLayout.setLibrary(layout.getLibrary());
		contentAreaLayout.setName(node.getName());
		this.wcmUtils.resolveResourceNode(contentAreaLayout, node);
		for (RestProperty property : node.getJcrProperties()) {
			if (" bpw:contentWidth".equals(property.getName())) {
				contentAreaLayout.setContentWidth(Integer.parseInt(property.getValues().get(0)));
				break;
			}
		}

		List<LayoutRow> rows = new ArrayList<>();

		node.getChildren().forEach(childNode -> {
			if (WcmUtils.checkNodeType(childNode, "bpw:contentAreaSidePanel")) {
				SidePane sidepane = new SidePane();
				for (RestProperty property : childNode.getJcrProperties()) {
					if ("bpw:isLeft".equals(property.getName())) {
						sidepane.setLeft(Boolean.parseBoolean(property.getValues().get(0)));
					} else if ("bpw:width".equals(property.getName())) {
						sidepane.setWidth(Integer.parseInt(property.getValues().get(0)));
					}
				}
				sidepane.setViewers(this.resolveResourceViewer(childNode));
				contentAreaLayout.setSidePane(sidepane);
			} else if (WcmUtils.checkNodeType(childNode, "bpw:layoutRow")) {
				LayoutRow row = this.resolveLayoutRow(childNode);
				rows.add(row);
			}
		});
		contentAreaLayout.setRows(rows.toArray(new LayoutRow[rows.size()]));
		return contentAreaLayout;
	}

	protected ResourceViewer[] resolveResourceViewer(RestNode restNode) {
		List<ResourceViewer> viewers = new ArrayList<>();

		restNode.getChildren().forEach(viewerNode -> {
			if (WcmUtils.checkNodeType(viewerNode, "bpw:resourceViewer")) {
				ResourceViewer viewer = new ResourceViewer();
				for (RestProperty property : viewerNode.getJcrProperties()) {
					if ("bpw:title".equals(property.getName())) {
						viewer.setTitle(property.getValues().get(0));
					} else if ("bpw:renderTemplateName".equals(property.getName())) {
						viewer.setRenderTemplate(property.getValues().get(0));
					} else if ("bpw:contentPath".equals(property.getName())) {
						viewer.setContentPath(property.getValues().toArray(new String[property.getValues().size()]));
					} else if ("bpw:contentParameter".equals(property.getName())) {
						viewer.setContentParameter(property.getValues().get(0));
					}
				}
				viewers.add(viewer);
			}
		});
		return viewers.toArray(new ResourceViewer[viewers.size()]);
	}

	protected LayoutRow resolveLayoutRow(RestNode restNode) {
		LayoutRow row = new LayoutRow();
		LayoutColumn columns[] = restNode.getChildren().stream()
				.filter(node -> WcmUtils.checkNodeType(node, "bpw:layoutColumn")).map(this::toLayoutColumn)
				.toArray(LayoutColumn[]::new);
		row.setColumns(columns);
		return row;
	}

	private LayoutColumn toLayoutColumn(RestNode node) {
		LayoutColumn column = new LayoutColumn();
		for (RestProperty property : node.getJcrProperties()) {
			if ("bpw:width".equals(property.getName())) {
				column.setWidth(Integer.parseInt(property.getValues().get(0)));
			}
		}
		column.setViewers(this.resolveResourceViewer(node));
		return column;
	}

	public Map<String, AuthoringTemplate> getAuthoringTemplates(String repository, String workspace,
			HttpServletRequest request) throws WcmRepositoryException {
		String baseUrl = RestHelper.repositoryUrl(request);
		Map<String, AuthoringTemplate> authoringTemplates = this
				.getAuthoringTemplateLibraries(repository, workspace, baseUrl)
				.flatMap(at -> this.getAuthoringTemplates(at, baseUrl))
				.collect(Collectors.toMap(
						at -> String.format(WcmConstants.WCM_AT_PATH_PATTERN, at.getLibrary(), at.getName()),
						Function.identity()));

		return authoringTemplates;
	}

	public Stream<AuthoringTemplate> getAuthoringTemplates(AuthoringTemplate at, String baseUrl)
			throws WcmRepositoryException {
		String absPath = String.format(WcmConstants.NODE_AT_ROOT_PATH_PATTERN, at.getLibrary());
		try {
			RestNode atNode = (RestNode) this.wcmItemHandler.item(baseUrl, at.getRepository(), at.getWorkspace(),
					absPath,
					WcmConstants.AT_JSON_FORM_DEPATH);

			return atNode.getChildren().stream().filter(this::isAuthortingTemplate).map(node -> this.wcmUtils
					.toAuthoringTemplate(node, at.getRepository(), at.getWorkspace(), at.getLibrary()));
		} catch (RepositoryException e) {
			throw new WcmRepositoryException(e, new WcmError(e.getMessage(), WcmErrors.GET_AT_ERROR, new String[] { absPath}));
		}
	}

	public Map<String, Form> getForms(String repository, String workspace, HttpServletRequest request)
			throws WcmRepositoryException {
		String baseUrl = RestHelper.repositoryUrl(request);
		Map<String, Form> forms = this.getFormLibraries(repository, workspace, baseUrl)
				.flatMap(form -> this.getForms(form, baseUrl))
				.collect(Collectors.toMap(
						form -> String.format(WcmConstants.WCM_FORM_PATH_PATTERN, form.getLibrary(), form.getName()),
						Function.identity()));

		return forms;
	}

	protected Stream<Form> getForms(Form form, String baseUrl) throws WcmRepositoryException {
		String absPath = String.format(WcmConstants.NODE_FORM_ROOT_PATH_PATTERN, form.getLibrary());
		try {
			RestNode formFolderNode = (RestNode) this.wcmItemHandler.item(baseUrl, form.getRepository(), form.getWorkspace(),
					absPath,
					WcmConstants.FORM_JSON_FORM_DEPATH);

			return formFolderNode.getChildren().stream().filter(this::isForm).map(
					node -> this.wcmUtils.toForm(node, form.getRepository(), form.getWorkspace(), form.getLibrary()));
		} catch (RepositoryException e) {
			throw new WcmRepositoryException(e, new WcmError(e.getMessage(), WcmErrors.GET_FORM_ERROR, new String[] { absPath}));
		}
	}

	public SiteConfig getSiteConfig(HttpServletRequest request, String repository, String workspace,
			String library, String siteConfigName) throws WcmRepositoryException {
		String absPath = String.format(WcmConstants.NODE_SITECONFIG_PATH_PATTERN, library, siteConfigName);
		try {
			String baseUrl = RestHelper.repositoryUrl(request);
			RestNode siteConfigNode = (RestNode) this.wcmItemHandler.item(baseUrl, repository, workspace, absPath,
					WcmConstants.SITE_CONFIG_DEPTH);
			SiteConfig siteConfig = this.toSiteConfig(repository, workspace, library, siteConfigNode);
			siteConfig.setRepository(repository);
			siteConfig.setWorkspace(workspace);
			siteConfig.setLibrary(library);
			return siteConfig;
		} catch (RepositoryException re) {
			throw new WcmRepositoryException(re, new WcmError(re.getMessage(), WcmErrors.GET_SITE_CONFIG_ERROR, new String[] { absPath}));
		}
	}

	public SiteConfig toSiteConfig(String repository, String workspace, String library, RestNode siteConfigNode) {

		// try {
		SiteConfig siteConfig = new SiteConfig();
		siteConfig.setWcmAuthority(WcmUtils.getWcmAuthority(null));
		siteConfig.setRepository(repository);
		siteConfig.setWorkspace(workspace);
		siteConfig.setLibrary(library);
		siteConfig.setName(siteConfigNode.getName());
		
		
		for (RestNode childNode : siteConfigNode.getChildren()) {
			if (WcmConstants.WCM_ITEM_ELEMENTS.equals(childNode.getName())) {
				for (RestProperty property : childNode.getJcrProperties()) {
//					if ("colorTheme".equals(property.getName())) {
//						siteConfig.setColorTheme(property.getValues().get(0));
//					} else 
					if ("customScrollbars".equals(property.getName())) {
						siteConfig.setCustomScrollbars(Boolean.parseBoolean(property.getValues().get(0)));
					} if ("animations".equals(property.getName())) {
						siteConfig.setAnimations(Boolean.parseBoolean(property.getValues().get(0)));
					} else if ("rootSiteArea".equals(property.getName())) {
						siteConfig.setRootSiteArea(property.getValues().get(0));
					} else if ("direction".equals(property.getName())) {
						siteConfig.setDirection(property.getValues().get(0));
					}
				}
				for (RestNode grandsonNode : childNode.getChildren()) {
					if ("themeColors".equals(grandsonNode.getName())) {
						ThemeColors themeColors = new ThemeColors();
						for (RestProperty property : grandsonNode.getJcrProperties()) {
							if ("main".equals(property.getName())) {
								themeColors.setMain(property.getValues().get(0));
							} else if ("navbar".equals(property.getName())) {
								themeColors.setNavbar(property.getValues().get(0));
							} else if ("toolbar".equals(property.getName())) {
								themeColors.setToolbar(property.getValues().get(0));
							} else if ("footer".equals(property.getName())) {
								themeColors.setFooter(property.getValues().get(0));
							}
						}
						siteConfig.setThemeColors(themeColors);
					} else if ("layout".equals(grandsonNode.getName())) {
						PageLayout layout = new PageLayout();
						siteConfig.setLayout(layout);
						for (RestProperty property : grandsonNode.getJcrProperties()) {
							if ("bpw:title".equals(property.getName())) {
								layout.setTitle(property.getValues().get(0));
							} else if ("bpw:mode".equals(property.getName())) {
								layout.setMode(property.getValues().get(0));
							} else if ("bpw:scroll".equals(property.getName())) {
								layout.setScroll(property.getValues().get(0));
							}
						}
						for (RestNode node : grandsonNode.getChildren()) {
							if ("navbar".equals(node.getName())) {
								NavBar navbar = new NavBar();
								layout.setNavbar(navbar);
								for (RestProperty property : node.getJcrProperties()) {
									if ("folded".equals(property.getName())) {
										navbar.setFolded(Boolean.valueOf(property.getValues().get(0)));
									} else if ("primaryBackground".equals(property.getName())) {
										navbar.setPrimaryBackground(property.getValues().get(0));
									} else if ("secondaryBackground".equals(property.getName())) {
										navbar.setSecondaryBackground(property.getValues().get(0));
									} else if ("variant".equals(property.getName())) {
										navbar.setVariant(property.getValues().get(0));
									} else if ("position".equals(property.getName())) {
										navbar.setPosition(property.getValues().get(0));
									} else if ("display".equals(property.getName())) {
										navbar.setDisplay(Boolean.valueOf(property.getValues().get(0)));
									} else if ("style".equals(property.getName())) {
										navbar.setStyle(property.getValues().get(0));
									}
								}
							} else if ("toolbar".equals(node.getName())) {
								Toolbar toolbar = new Toolbar();
								layout.setToolbar(toolbar);
								for (RestProperty property : node.getJcrProperties()) {
									if ("customBackgroundColor".equals(property.getName())) {
										toolbar.setCustomBackgroundColor(Boolean.valueOf(property.getValues().get(0)));
									} else if ("background".equals(property.getName())) {
										toolbar.setBackground(property.getValues().get(0));
									} else if ("position".equals(property.getName())) {
										toolbar.setPosition(property.getValues().get(0));
									} else if ("style".equals(property.getName())) {
										toolbar.setStyle(property.getValues().get(0));
									} else if ("display".equals(property.getName())) {
										toolbar.setDisplay(Boolean.valueOf(property.getValues().get(0)));
									}
								}
							} else if ("footer".equals(node.getName())) {
								Footer footer = new Footer();
								layout.setFooter(footer);
								for (RestProperty property : node.getJcrProperties()) {
									if ("customBackgroundColor".equals(property.getName())) {
										footer.setCustomBackgroundColor(Boolean.valueOf(property.getValues().get(0)));
									} else if ("background".equals(property.getName())) {
										footer.setBackground(property.getValues().get(0));
									} else if ("position".equals(property.getName())) {
										footer.setPosition(property.getValues().get(0));
									} else if ("display".equals(property.getName())) {
										footer.setDisplay(Boolean.valueOf(property.getValues().get(0)));
									} else if ("style".equals(property.getName())) {
										footer.setStyle(property.getValues().get(0));
									} 
								}
							} else if ("leftSidePanel".equals(node.getName())) {
								SidePanel sidePanel = new SidePanel();
								layout.setLeftSidePanel(sidePanel);
								for (RestProperty property : node.getJcrProperties()) {
									if ("display".equals(property.getName())) {
										sidePanel.setDisplay(Boolean.valueOf(property.getValues().get(0)));
									}
									break;
								}
							} else if ("rightSidePanel".equals(node.getName())) {
								SidePanel sidePanel = new SidePanel();
								layout.setRightSidePanel(sidePanel);
								for (RestProperty property : node.getJcrProperties()) {
									if ("display".equals(property.getName())) {
										sidePanel.setDisplay(Boolean.valueOf(property.getValues().get(0)));
									}
									break;
								}
							}
						}
						// break;
					}
				}
			} else if (WcmConstants.WCM_ITEM_PROPERTIES.equals(childNode.getName())) {
				this.wcmUtils.resolveResourceNode(siteConfig, childNode);
			} 
		}
		return siteConfig;
	}

	protected Map<String, SiteArea> getSiteAreas(String repository, String workspace, String library,
			String rootSiteArea, String baseUrl) throws RepositoryException {
		RestNode saNode = (RestNode) this.wcmItemHandler.item(baseUrl, repository, workspace,
				String.format(WcmConstants.NODE_REL_PATH_PATTERN, library, rootSiteArea), WcmConstants.SITE_AREA_DEPTH);
		Map<String, SiteArea> siteAreas = new HashMap<>();
		for (RestNode node : saNode.getChildren()) {
			if (this.isSiteArea(node)) {
				this.toSiteArea(node, siteAreas);
			}
		}
		return siteAreas;
	}

	private SiteArea toSiteArea(RestNode saNode, Map<String, SiteArea> siteAreas) {
		SiteArea sa = new SiteArea();
		this.loadSiteArea(saNode, sa);

		for (RestNode node : saNode.getChildren()) {
			if (this.isSiteArea(node)) {
				this.toSiteArea(node, siteAreas);
			}
		}
		String url = (String)sa.getElements().get("url");
		if (StringUtils.hasText(url)) {
			siteAreas.put(url.replace("/", "~"), sa);
		}
		return sa;
	}

	public void loadSiteArea(RestNode saNode, SiteArea sa) {
		Map<String, Object> elements = new HashMap<>();
		ResourceMixin resourceMixin = new ResourceMixin();
		sa.setProperties(resourceMixin);
		sa.setElements(elements);
		for (RestNode childNode : saNode.getChildren()) {
			if (WcmConstants.WCM_ITEM_ELEMENTS.equals(childNode.getName())) {
				for (RestProperty property : childNode.getJcrProperties()) {
					if (property.getName().indexOf(":") < 0) {
						// elements.put(this.getSiteAreaPropertyName(property.getName()), property.getValues().get(0));
						elements.put(property.getName(), property.getValues().get(0));
					}
				}
				
				for (RestNode node : childNode.getChildren()) {
					if (WcmUtils.checkNodeType(node, "bpw:navigationBadge")
							&& ("bpw:badge".equals(node.getName()))) {
						NavigationBadge badge = new NavigationBadge();
						for (RestProperty property : node.getJcrProperties()) {
							if ("bpw:title".equals(property.getName())) {
								badge.setTitle(property.getValues().get(0));
							} else if ("bpw:translate".equals(property.getName())) {
								badge.setTranslate(property.getValues().get(0));
							} else if ("bpw:bg".equals(property.getName())) {
								badge.setBg(property.getValues().get(0));
							} else if ("bpw:fg".equals(property.getName())) {
								badge.setFg(property.getValues().get(0));
							}
						}
						sa.setBadge(badge);
						break;
					}
				}
			} else if (WcmConstants.WCM_ITEM_PROPERTIES.equals(childNode.getName())) {
				this.wcmUtils.resolveResourceMixin(resourceMixin, childNode);  
			} else if (WcmUtils.checkNodeType(childNode, "bpw:siteAreaLayout")
					&& ("siteAreaLayout".equals(childNode.getName()))) {
				SiteAreaLayout siteAreaLayout = new SiteAreaLayout();
				List<LayoutRow> rows = new ArrayList<>();
				for (RestProperty property : childNode.getJcrProperties()) {
					if ("bpw:contentWidth".equals(property.getName())) {
						siteAreaLayout.setContentWidth(Integer.parseInt(property.getValues().get(0)));
						break;
					}
				}
				for (RestNode layoutNode : childNode.getChildren()) {
					if (WcmUtils.checkNodeType(layoutNode, "bpw:contentAreaSidePanel")
							&& "sidePane".equals(layoutNode.getName())) {
						SidePane sidePane = new SidePane();
						for (RestProperty property : layoutNode.getJcrProperties()) {
							if ("bpw:isLeft".equals(property.getName())) {
								sidePane.setLeft(Boolean.parseBoolean(property.getValues().get(0)));
							} else if ("bpw:width".equals(property.getName())) {
								sidePane.setWidth(Integer.parseInt(property.getValues().get(0)));
							}
						}
						sidePane.setViewers(this.resolveResourceViewer(layoutNode));
						siteAreaLayout.setSidePane(sidePane);
					} else if (WcmUtils.checkNodeType(layoutNode, "bpw:layoutRow")) {
						LayoutRow row = this.resolveLayoutRow(layoutNode);
						rows.add(row);
					}
				}
				siteAreaLayout.setRows(rows.toArray(new LayoutRow[rows.size()]));
				sa.setSiteAreaLayout(siteAreaLayout);
			} else if (WcmUtils.checkNodeType(childNode, "bpw:properties") && ("bpw:metaData".equals(childNode.getName()))) {
				WcmProperties metadata = new WcmProperties();
				List<WcmProperty> propertyList = new ArrayList<>();
				for (RestNode kvNode : childNode.getChildren()) {
					if (WcmUtils.checkNodeType(kvNode, "bpw:property")) {
						WcmProperty wcmProperty = new WcmProperty();
						for (RestProperty property : kvNode.getJcrProperties()) {
							if ("bpw:name".equals(property.getName())) {
								wcmProperty.setName(property.getValues().get(0));
							} else if ("bpw:value".equals(property.getName())) {
								wcmProperty.setValue(property.getValues().get(0));
							}
						}
						propertyList.add(wcmProperty);
					}
				}
				metadata.setProperties(propertyList.toArray(new WcmProperty[propertyList.size()]));
				sa.setMetadata(metadata);
			} else if (WcmUtils.checkNodeType(childNode, "bpw:pageSearchData")
					&& ("bpw:searchData".equals(childNode.getName()))) {
				SearchData searchData = new SearchData();
				for (RestProperty property : childNode.getJcrProperties()) {
					if ("description".equals(property.getName())) {
						searchData.setDescription(property.getValues().get(0));
					} else if ("keywords".equals(property.getName())) {
						searchData.setKeywords(property.getValues().toArray(new String[property.getValues().size()]));
					}
				}
				sa.setSearchData(searchData);
			}
		} 
	}

	public Navigation[] getNavigations(String baseUrl, String repository, String workspace, String library,
			String rootSiteArea) throws RepositoryException {
		RestNode siteArea = (RestNode) this.wcmItemHandler.item(baseUrl, repository, workspace,
				String.format(WcmConstants.NODE_REL_PATH_PATTERN, library, rootSiteArea),
				WcmConstants.NAVIGATION_DEPTH);

		Navigation[] navigation = siteArea.getChildren().stream().filter(this::isSitePage)
				.map(node -> this.toNavigation(node)).toArray(Navigation[]::new);
		return navigation;
	}

	public ControlField[] getControlField(String repository, String workspace, HttpServletRequest request)
			throws RepositoryException {
		String baseUrl = RestHelper.repositoryUrl(request);
		RestNode controlFieldFolder = (RestNode) this.wcmItemHandler.item(baseUrl, repository, workspace,
				WcmConstants.NODE_CONTROL_FIELD_ROOT, WcmConstants.CONTROL_FIELD_DEPTH);
		ControlField[] ControlFileds = controlFieldFolder.getChildren().stream().filter(this::isControlField)
				.map(this::toControlField).toArray(ControlField[]::new);

		return ControlFileds;
	}

	private ControlField toControlField(RestNode node) {
		ControlField controlField = new ControlField();
		controlField.setName(node.getName());
		for (RestProperty restProperty : node.getJcrProperties()) {
			if ("bpw:title".equals(restProperty.getName())) {
				controlField.setTitle(restProperty.getValues().get(0));
			} else if ("bpw:icon".equals(restProperty.getName())) {
				controlField.setIcon(restProperty.getValues().get(0));
			} else if ("bpw:hint".equals(restProperty.getName())) {
				controlField.setHint(restProperty.getValues().get(0));
			}

		}
		return controlField;
	}
	
	private NavigationItem toNavigation(RestNode siteArea) {
		return this.toNavigation(siteArea, 0);
	}
	
	private NavigationItem toNavigation(RestNode siteArea, int level) {
		
		NavigationItem navigation = (level == 0) ? new Navigation() : new NavigationItem();
		
		for (RestNode childNode : siteArea.getChildren()) {
			if (WcmConstants.WCM_ITEM_ELEMENTS.equals(childNode.getName())) {
				for (RestProperty property : childNode.getJcrProperties()) {
					if ("navigationId".equals(property.getName())) {
						navigation.setId(property.getValues().get(0));
					} else if ("navigationType".equals(property.getName())) {
						navigation.setType(NavigationType.valueOf(property.getValues().get(0)));
					} else if ("translate".equals(property.getName())) {
						navigation.setTranslate(property.getValues().get(0));
					} else if ("icon".equals(property.getName())) {
						navigation.setIcon(property.getValues().get(0));
					} else if ("url".equals(property.getName())) {
						navigation.setUrl(property.getValues().get(0));
					} 
				}
				
				for (RestNode node : childNode.getChildren()) {
					if (WcmUtils.checkNodeType(node, "bpw:navigationBadge")) {
						NavigationBadge badge = new NavigationBadge();
						for (RestProperty property : node.getJcrProperties()) {
							if ("bpw:title".equals(property.getName())) {
								badge.setTitle(property.getValues().get(0));
							} else if ("bpw:translate".equals(property.getName())) {
								badge.setTranslate(property.getValues().get(0));
							} else if ("bpw:bg".equals(property.getName())) {
								badge.setBg(property.getValues().get(0));
							} else if ("bpw:fg".equals(property.getName())) {
								badge.setFg(property.getValues().get(0));
							}
						}
						navigation.setBadge(badge);
					}
				}
				
			} else if (WcmConstants.WCM_ITEM_PROPERTIES.equals(childNode.getName())) {
				for (RestProperty property : childNode.getJcrProperties()) {
					if ("bpw:title".equals(property.getName())) {
						navigation.setTitle(property.getValues().get(0));
						break;
					}
				}
			}
		}
		if (level <= 2) {
			NavigationItem[] navigationItems = siteArea.getChildren().stream().filter(this::isSitePage)
					.map(node -> this.toNavigation(node, level + 1)).toArray(NavigationItem[]::new);
			navigation.setChildren(navigationItems);
		}
		return navigation;
	}

	protected Stream<QueryStatement> doGetQueryStatements(QueryStatement query, String baseUrl)
			throws WcmRepositoryException {
		String absPath = String.format(WcmConstants.NODE_QUERY_ROOT_PATH_PATTERN, query.getLibrary());
		try {
			RestNode queryStatementNode = (RestNode) this.wcmItemHandler.item(baseUrl, query.getRepository(), query.getWorkspace(),
					absPath,
					WcmConstants.QUERY_STMT_DEPTH);

			return queryStatementNode.getChildren().stream().filter(this::isQueryStatement).map(node -> this.toQueryStatement(node,
					query.getRepository(), query.getWorkspace(), query.getLibrary()));
		} catch (RepositoryException e) {
			throw new WcmRepositoryException(e, new WcmError(e.getMessage(), WcmErrors.GET_QUERY_ERROR, null));
		}
	}

	protected QueryStatement toQueryStatement(RestNode restNode, String repository, String workspace, String library) {
		QueryStatement queryStatement = new QueryStatement();
		queryStatement.setWcmAuthority(WcmUtils.getWcmAuthority(null));
		queryStatement.setRepository(repository);
		queryStatement.setWorkspace(workspace);
		queryStatement.setLibrary(library);

		queryStatement.setName(restNode.getName());
		restNode.getChildren().forEach(node -> {
			if (WcmUtils.isElementFolderNode(node)) {
				for (RestProperty restProperty : node.getJcrProperties()) {
					if ("query".equals(restProperty.getName())) {
						queryStatement.setQuery(restProperty.getValues().get(0));
					} else if ("columns".equals(restProperty.getName())) {
						queryStatement
								.setColumns(restProperty.getValues().toArray(new String[restProperty.getValues().size()]));
					}
				}
			} else if (WcmUtils.isPropertyFolderNode(node)) {
				for (RestProperty restProperty : node.getJcrProperties()) {
					if ("bpw:title".equals(restProperty.getName())) {
						queryStatement.setTitle(restProperty.getValues().get(0));
						break;
					}
				}
			} 
		});
		return queryStatement;
	}
	
	public QueryStatement[] loadQueryStatements(String repository, String workspace, HttpServletRequest request)
			throws WcmRepositoryException {

		String baseUrl = RestHelper.repositoryUrl(request);

		QueryStatement[] queryStatements = this.getQueryLibraries(repository, workspace, baseUrl)
				.flatMap(library -> this.doGetQueryStatements(library, baseUrl)).toArray(QueryStatement[]::new);

		return queryStatements;
	}

	protected Stream<QueryStatement> getQueryLibraries(String repository, String workspace, String baseUrl)
			throws WcmRepositoryException {
		try {
			RestNode bpwizardNode = (RestNode) this.wcmItemHandler.item(baseUrl, repository, workspace,
					WcmConstants.NODE_ROOT_PATH, WcmConstants.READ_DEPTH_DEFAULT);
			return bpwizardNode.getChildren().stream().filter(this::notSystemLibrary)
					.map(node -> toQueryStatementWithLibrary(node, repository, workspace));
		} catch (RepositoryException e) {
			throw new WcmRepositoryException(e, new WcmError(e.getMessage(), WcmErrors.GET_NODE_ERROR, null));
		}
	}

	protected QueryStatement toQueryStatementWithLibrary(RestNode node, String repository, String workspace) {
		QueryStatement queryStatementWithLibrary = new QueryStatement();
		queryStatementWithLibrary.setRepository(repository);
		queryStatementWithLibrary.setWorkspace(workspace);
		queryStatementWithLibrary.setLibrary(node.getName());
		return queryStatementWithLibrary;
	}

	protected Stream<String> getLibraries(String repository, String workspace, String baseUrl)
			throws WcmRepositoryException {
		try {
			RestNode bpwizardNode = (RestNode) this.wcmItemHandler.item(baseUrl, repository, workspace,
					WcmConstants.NODE_ROOT_PATH, WcmConstants.READ_DEPTH_DEFAULT);
			return bpwizardNode.getChildren().stream().filter(this::notSystemLibrary).map(RestNode::getName);
		} catch (RepositoryException e) {
			throw new WcmRepositoryException(e, new WcmError(e.getMessage(), WcmErrors.GET_NODE_ERROR, null));
		}
	}
	
	public ContentItem getContentItem(
		    String repository,
		    String workspace,
		    String wcmPath,
		    HttpServletRequest request) 
		    throws WcmRepositoryException {
		if (logger.isDebugEnabled()) {
			logger.debug("Entry");
		}
		String absPath = wcmPath.startsWith("/library")? wcmPath : WcmUtils.nodePath(wcmPath);
		try {
			String baseUrl = RestHelper.repositoryUrl(request);
			
			RestNode contentItemNode = (RestNode) this.wcmItemHandler.item(baseUrl, repository, workspace,
					absPath, WcmConstants.CONTENT_ITEM_DEPATH);
			
			ContentItem contentItem = this.toContentItem(contentItemNode, repository, workspace, wcmPath, request);
			if (logger.isDebugEnabled()) {
				logger.debug("Exit");
			}
			return contentItem;
		} catch (WcmRepositoryException e ) {
			logger.error("Failed to get content item", e);
			throw e;
		} catch (Throwable t) {
			logger.error("Failed to get content item", t);
	    	throw new WcmRepositoryException(t, WcmError.UNEXPECTED_ERROR);
		}
	}
	public ContentItem toContentItem(RestNode contentItemNode, String repository, 
			String workspace, String wcmPath, HttpServletRequest request) {
	    
		ContentItem contentItem = new ContentItem(); 
		contentItem.setWcmAuthority(WcmUtils.getWcmAuthority(wcmPath));
		contentItem.setRepository(repository);
		contentItem.setWorkspace(workspace);
		contentItem.setWcmPath(wcmPath.startsWith("/") ? wcmPath : "/" + wcmPath);
		contentItem.setId(contentItemNode.getId());
		ContentItemProperties contentItemProperties = new ContentItemProperties();
		contentItem.setProperties(contentItemProperties);
		for (RestProperty property: contentItemNode.getJcrProperties()) {				
			if ("jcr:primaryType".equals(property.getName())) {
				contentItem.setNodeType(property.getValues().get(0));
			} else if ("bpw:authoringTemplate".equals(property.getName())) {
				contentItem.setAuthoringTemplate(property.getValues().get(0));
			} 
		}
		//Resolve properties first
		for (RestNode node: contentItemNode.getChildren()) {
			if (WcmConstants.WCM_ITEM_PROPERTIES.equals(node.getName()) || 
					WcmUtils.checkNodeType(node, WcmConstants.JCR_TYPE_PROPERTY_FOLDER)) {					
				contentItemProperties.setName(contentItemNode.getName());					
				for (RestProperty property: node.getJcrProperties()) {
					if ("bpw:categories".equals(property.getName())) {
						contentItemProperties.setCategories(property.getValues().toArray(new String[property.getValues().size()]));
					} else if ("bpw:name".equals(property.getName())) {
						contentItemProperties.setName(property.getValues().get(0));
					} else if ("bpw:title".equals(property.getName())) {
						contentItemProperties.setTitle(property.getValues().get(0));
					} else if ("bpw:description".equals(property.getName())) {
						contentItemProperties.setDescription(property.getValues().get(0));
					} else if ("bpw:author".equals(property.getName())) {
						contentItemProperties.setAuthor(property.getValues().get(0));
					} 
				}
				break;
			}
		}
		AuthoringTemplate at = this.getAuthoringTemplate(repository, workspace, contentItem.getAuthoringTemplate(), request);
		
		for (RestNode node: contentItemNode.getChildren()) {
			if (WcmUtils.checkNodeType(node, "bpw:workflowNode")) {
				WorkflowNode workflowNode = new WorkflowNode();
				contentItem.setWorkflow(workflowNode);
				this.resolveWorkflowNode(workflowNode, node);
			} else if (WcmConstants.WCM_ITEM_ELEMENTS.equals(node.getName()) || 
					WcmUtils.checkNodeType(node, WcmUtils.getElementFolderType(at.getLibrary(), at.getName()))) {
				// Map<String, JsonNode> elements = new HashMap<>();
				Map<String, Object> elements = new HashMap<>();
				contentItem.setElements(elements);
				
				for (RestProperty property : node.getJcrProperties()) {
					
					FormControl formControl = at.getElements().get(property.getName());
					if (formControl == null) {
						continue;
					}
					if ("integer".equals(formControl.getDataType()) 
							|| "number".equals(formControl.getDataType()) 
							|| "string".equals(formControl.getDataType())
							|| "boolean".equals(formControl.getDataType())) {
						if (formControl.isMultiple()) {					
							String values[] = property.getValues().toArray(new String[property.getValues().size()]);
							elements.put(property.getName(), WcmUtils.toArrayNode(values));
						} else {
							elements.put(property.getName(), JsonUtils.createTextNode(property.getValues().get(0)));
						}
					} else if ("object".equals(formControl.getDataType())) {
						//TODO
						if (formControl.isMultiple()) {
							String values[] = property.getValues().toArray(new String[property.getValues().size()]);
							try {
							elements.put(property.getName(), JsonUtils.readTree(values));
							} catch(JsonProcessingException e) {
								logger.error("Failed to retrieve content item", e);
								throw new WcmRepositoryException(WcmError.WCM_ERROR);
							}
						} else {
							elements.put(property.getName(), JsonUtils.readTree(property.getValues().get(0)));
						}
					} else if ("array".equals(formControl.getDataType())) {
						//TODO
						elements.put(property.getName(), JsonUtils.readTree(property.getValues().get(0)));
					}
				}
			} else if (WcmUtils.checkNodeType(node, "bpw:acl")) {
				AccessControlEntry acl = new AccessControlEntry();
				contentItem.setAcl(acl);
				for (RestProperty property: node.getJcrProperties()) {
					if ("bpw:viewers".equals(property.getName())) {
						acl.setViewers(property.getValues().toArray(new String[property.getValues().size()]));
					} else if ("bpw:editors".equals(property.getName())) {
						acl.setEditors(property.getValues().toArray(new String[property.getValues().size()]));
					} else if ("bpw:admins".equals(property.getName())) {
						acl.setAdmins(property.getValues().toArray(new String[property.getValues().size()]));
					} else if ("bpw:reviewers".equals(property.getName())) {
						acl.setReviewers(property.getValues().toArray(new String[property.getValues().size()]));
					} 
				}
			} else if (WcmUtils.checkNodeType(node, "bpw:metaData")) {
				WcmProperties wcmProperties = new WcmProperties();
				contentItem.setMetadata(wcmProperties);
				List<WcmProperty>  properties = new ArrayList<>();
				wcmProperties.setProperties(new WcmProperty[properties.size()]);
				for (RestNode metaDataNode: node.getChildren()) {
					if (WcmUtils.checkNodeType(node, "bpw:property")) {
						WcmProperty wcmProperty = new WcmProperty();
						properties.add(wcmProperty);
						for (RestProperty restProperty: metaDataNode.getJcrProperties()) {
							if ("bpw:name".equals(restProperty.getName())) {
								wcmProperty.setName(restProperty.getValues().get(0));
							} else if ("bpw:value".equals(restProperty.getName())) {
								wcmProperty.setValue(restProperty.getValues().get(0));
							}
						}
					}
				}
			} else if (WcmUtils.checkNodeType(node, "bpw:searchData")) {
				SearchData searchData = new SearchData();
				contentItem.setSearchData(searchData);
				for (RestProperty property: node.getJcrProperties()) {
					if ("description".equals(property.getName())) {
						searchData.setDescription(property.getValues().get(0));
					} else if ("keywords".equals(property.getName())) {
						searchData.setKeywords(property.getValues().toArray(new String[property.getValues().size()]));
					} 
				}
			}
		}
		return contentItem;
	}
	
	public void setWorkflowStage(ContentItem contentItem, String stage) {
		WorkflowNode workflowNode = contentItem.getWorkflow();
		if (workflowNode == null) {
			workflowNode = new WorkflowNode();
			contentItem.setWorkflow(workflowNode);
		}
		workflowNode.setWorkflowStage(stage);
	}
	
	public <T> T readJson(InputStream requestBody, Class<T> valueType) throws IOException {
        return new ObjectMapper().readValue(requestBody, valueType);
    }
	
	public void createLibrary(
			Library library, 
			HttpServletRequest request) 
			throws WcmRepositoryException {
		if (logger.isDebugEnabled()) {
			logger.debug("Entry");
		}
		try {
			String repositoryName = library.getRepository();
			String baseUrl = RestHelper.repositoryUrl(request);
			String path = String.format(WcmConstants.NODE_LIB_PATH_PATTERN, library.getName());
			this.wcmItemHandler.addItem(
					WcmEventEntry.WcmItemType.library,
					baseUrl, 
					repositoryName,
					library.getWorkspace(),
					path, 
					library.toJson());
			
			if (logger.isDebugEnabled()) {
				logger.debug("Exit");
			}
	
		} catch (WcmRepositoryException e ) {
			logger.error("Failed to create library", e);
			throw e;
		} catch (Throwable t) {
			logger.error("Failed to create library", t);
			throw new WcmRepositoryException(t, WcmError.UNEXPECTED_ERROR);
		}	
	}
	public void loadQueries( HttpServletRequest request,
    		String repository,
    		String workspace,
    		InputStream inputStream) throws WcmRepositoryException {
    	logger.debug("Entering ...");
    	try {
    		com.bpwizard.wcm.repo.rest.jcr.model.Query[] queries = this.readJson(
    				inputStream, 
    				com.bpwizard.wcm.repo.rest.jcr.model.Query[].class);
    		for (com.bpwizard.wcm.repo.rest.jcr.model.Query query: queries) {
    			
    			this.createQueryStatement(toQueryStatement(query, repository, workspace), request);
    		}
	    	logger.debug("Exiting ...");
    	} catch (Throwable t) {
    		throw new WcmRepositoryException(t, new WcmError(t.getMessage(), WcmErrors.MODESHAPE_POST_ITEMS, null));
    	} 
    }
	
	
	public void createQueryStatement(QueryStatement query, HttpServletRequest request) 
			throws WcmRepositoryException {
		try {
			String repositoryName = query.getRepository();
			
			try {
				QueryManager qrm = this.repositoryManager.getSession(repositoryName, WcmConstants.DEFAULT_WS).getWorkspace().getQueryManager();
				javax.jcr.query.Query jcrQuery = qrm.createQuery(query.getQuery(), Query.JCR_SQL2);
				QueryResult jcrResult = jcrQuery.execute();
				String columnNames[] = jcrResult.getColumnNames();
				query.setColumns(columnNames);
			} catch (Exception e) {
				logger.error("JCR Query Error.", e);
				String message = (e instanceof InvalidQueryException) ? ((InvalidQueryException)e).getMessage() : "Invalid JCR Query";
				throw new WcmRepositoryException(e, new WcmError(
						SpringExceptionUtils.getMessage("com.bpwizard.modeshape.invalid_query", message), 
						WcmErrors.INVALID_JCR_QUERY, 
						null));
			}
			ObjectNode qJson = (ObjectNode) query.toJson();
			// javax.jcr.query.qom.QueryObjectModel qom = null
			String baseUrl = RestHelper.repositoryUrl(request);
			String path = String.format(WcmConstants.NODE_QUERY_PATH_PATTERN, query.getLibrary(), query.getName());
			
			this.wcmItemHandler.addItem(WcmEventEntry.WcmItemType.query, baseUrl,  repositoryName, WcmConstants.DEFAULT_WS, path, qJson);
			if (logger.isDebugEnabled()) {
				logger.debug("Exit");
			}
			
		} catch (WcmRepositoryException e ) {
			logger.error("Failed to create query statement", e);
			throw e;
		} catch (RepositoryException re) { 
			logger.error("Failed to create query statement", re);
			throw new WcmRepositoryException(re, new WcmError(re.getMessage(), WcmErrors.CREATE_QUERY_ERROR, null));
	    } catch (Throwable t) {
	    	logger.error("Failed to create query statement", t);
			throw new WcmRepositoryException(t, WcmError.UNEXPECTED_ERROR);
		}	
	}
	
	private QueryStatement toQueryStatement(com.bpwizard.wcm.repo.rest.jcr.model.Query query, String repository, String workspace) {
		QueryStatement queryStatement = new QueryStatement();
		queryStatement.setRepository(repository);
		queryStatement.setWorkspace(workspace);
		queryStatement.setQuery(query.getQuery());
		queryStatement.setName(query.getName());
		queryStatement.setLibrary(query.getLibrary());
		return queryStatement;
	}	
}

