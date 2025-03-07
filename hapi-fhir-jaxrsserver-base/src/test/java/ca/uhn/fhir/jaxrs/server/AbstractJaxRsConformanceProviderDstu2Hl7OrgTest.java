package ca.uhn.fhir.jaxrs.server;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jaxrs.server.test.TestJaxRsDummyPatientProviderDstu2Hl7Org;
import ca.uhn.fhir.jaxrs.server.test.TestJaxRsMockPatientRestProviderDstu2Hl7Org;
import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.rest.server.IResourceProvider;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.jboss.resteasy.specimpl.ResteasyHttpHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AbstractJaxRsConformanceProviderDstu2Hl7OrgTest {
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(AbstractJaxRsConformanceProviderDstu2Hl7OrgTest.class);


	private static final String BASEURI = "http://basiuri";
	private static final String REQUESTURI = BASEURI + "/metadata";
	AbstractJaxRsConformanceProvider provider;
	private ConcurrentHashMap<Class<? extends IResourceProvider>, IResourceProvider> providers;
	private ResteasyHttpHeaders headers;
	private MultivaluedHashMap<String, String> queryParameters;

	@BeforeEach
	public void setUp() throws Exception {
		// uri info
		queryParameters = new MultivaluedHashMap<>();
		// headers
//		headers = new ContainerRequest(new URI(BASEURI), new URI(REQUESTURI), HttpMethod.GET, null,
//				new MapPropertiesDelegate());
		headers = new ResteasyHttpHeaders(queryParameters);


		providers = new ConcurrentHashMap<Class<? extends IResourceProvider>, IResourceProvider>();
		provider = createConformanceProvider(providers);
	}

	@Test
	public void testConformance() throws Exception {
		providers.put(AbstractJaxRsConformanceProvider.class, provider);
		providers.put(TestJaxRsDummyPatientProviderDstu2Hl7Org.class, new TestJaxRsDummyPatientProviderDstu2Hl7Org());
		Response response = createConformanceProvider(providers).conformance();
		ourLog.info(response.toString());
	}

	@Test
	public void testConformanceUsingOptions() throws Exception {
		providers.put(AbstractJaxRsConformanceProvider.class, provider);
		providers.put(TestJaxRsDummyPatientProviderDstu2Hl7Org.class, new TestJaxRsDummyPatientProviderDstu2Hl7Org());
		Response response = createConformanceProvider(providers).conformanceUsingOptions();
		ourLog.info(response.toString());
	}

	@Test
	public void testConformanceWithMethods() throws Exception {
		providers.put(AbstractJaxRsConformanceProvider.class, provider);
		providers.put(TestJaxRsMockPatientRestProviderDstu2Hl7Org.class, new TestJaxRsMockPatientRestProviderDstu2Hl7Org());
		Response response = createConformanceProvider(providers).conformance();
		assertEquals(Constants.STATUS_HTTP_200_OK, response.getStatus());
		assertThat(response.getEntity().toString()).contains("\"type\": \"Patient\"");
		assertThat(response.getEntity().toString()).contains("someCustomOperation");
		ourLog.info(response.toString());
		ourLog.info(response.getEntity().toString());
	}

	@Test
	public void testConformanceInXml() throws Exception {
		queryParameters.put(Constants.PARAM_FORMAT, Arrays.asList(Constants.CT_XML));
		providers.put(AbstractJaxRsConformanceProvider.class, provider);
		providers.put(TestJaxRsMockPatientRestProviderDstu2Hl7Org.class, new TestJaxRsMockPatientRestProviderDstu2Hl7Org());
		Response response = createConformanceProvider(providers).conformance();
		assertEquals(Constants.STATUS_HTTP_200_OK, response.getStatus());
		ourLog.info(response.getEntity().toString());
		assertThat(response.getEntity().toString()).contains(" <type value=\"Patient\"/>");
		assertThat(response.getEntity().toString()).contains("someCustomOperation");
		ourLog.info(response.getEntity().toString());
	}

	private AbstractJaxRsConformanceProvider createConformanceProvider(final ConcurrentHashMap<Class<? extends IResourceProvider>, IResourceProvider> providers)
			throws Exception {
		AbstractJaxRsConformanceProvider result = new AbstractJaxRsConformanceProvider(FhirContext.forDstu2Hl7Org(), null, null, null) {
			@Override
			protected ConcurrentHashMap<Class<? extends IResourceProvider>, IResourceProvider> getProviders() {
				return providers;
			}
		};
		// mocks
		UriInfo uriInfo = mock(UriInfo.class);
		when(uriInfo.getQueryParameters()).thenReturn(queryParameters);
		when(uriInfo.getBaseUri()).thenReturn(new URI(BASEURI));
		when(uriInfo.getRequestUri()).thenReturn(new URI(BASEURI + "/foo"));
		result.setUriInfo(uriInfo);
		result.setHeaders(headers);
		result.buildCapabilityStatement();
		return result;
	}

}

