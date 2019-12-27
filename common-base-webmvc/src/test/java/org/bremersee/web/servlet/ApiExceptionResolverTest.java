/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bremersee.web.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.bremersee.exception.RestApiExceptionMapper;
import org.bremersee.exception.ServiceException;
import org.bremersee.exception.model.Handler;
import org.bremersee.exception.model.RestApiException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import org.springframework.web.util.WebUtils;

/**
 * The api exception resolver test.
 *
 * @author Christian Bremer
 */
public class ApiExceptionResolverTest {

  private static ServiceException cause = ServiceException.builder()
      .httpStatus(401)
      .reason("You are not authorized")
      .errorCode("TEST:4711")
      .build();

  private static ServiceException exception = ServiceException.builder()
      .httpStatus(cause.status())
      .reason("Authorization failed.")
      .cause(cause)
      .build();

  private static RestApiException expectedCause = RestApiException.builder()
      .message(cause.getMessage())
      .errorCode(cause.getErrorCode())
      .build();

  private static RestApiException expected = RestApiException.builder()
      .cause(expectedCause)
      .errorCodeInherited(true)
      .errorCode(expectedCause.getErrorCode())
      .handler(Handler.builder()
          .className(TestHandler.class.getName())
          .methodName("testMethod")
          .methodParameterTypes(Arrays.asList(
              UUID.class.getName(),
              String.class.getName(),
              int.class.getName()))
          .build())
      .build();

  private static ApiExceptionResolver exceptionResolver;

  /**
   * Setup test.
   */
  @BeforeClass
  public static void setup() {

    final RestApiExceptionMapper mapper = mock(RestApiExceptionMapper.class);
    when(mapper.getApiPaths())
        .thenReturn(Collections.singletonList("/api/resource"));
    when(mapper.detectHttpStatus(any(), any()))
        .thenReturn(HttpStatus.UNAUTHORIZED);
    when(mapper.build(any(Throwable.class), anyString(), any(HandlerMethod.class)))
        .thenReturn(expected);

    exceptionResolver = new ApiExceptionResolver(mapper);
  }

  /**
   * Test resolve exception with json content.
   *
   * @throws Exception the exception
   */
  @Test
  public void testResolveExceptionWithJsonContent() throws Exception {

    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRequestURI()).thenReturn("/api/resource");
    when(request.getServletPath()).thenReturn("/api/resource");
    when(request.getHeader(eq(HttpHeaders.ACCEPT)))
        .thenReturn(MediaType.APPLICATION_JSON_VALUE);
    when(request.getAttribute(eq(WebUtils.INCLUDE_REQUEST_URI_ATTRIBUTE)))
        .thenReturn(null);
    when(request.getAttribute(eq(WebUtils.ERROR_STATUS_CODE_ATTRIBUTE)))
        .thenReturn(exception.status());

    HttpServletResponse response = mock(HttpServletResponse.class);
    when(response.getStatus()).thenReturn(exception.status());

    HandlerMethod handler = mock(HandlerMethod.class);
    when(handler.getBean()).thenReturn(new TestHandler());
    when(handler.getMethod())
        .thenReturn(
            TestHandler.class.getMethod("testMethod", UUID.class, String.class, int.class));

    ModelAndView mv = exceptionResolver.resolveException(request, response, handler, exception);

    assertNotNull(mv);
    assertEquals(HttpStatus.UNAUTHORIZED, mv.getStatus());

    assertNotNull(mv.getView());
    assertTrue(mv.getView() instanceof MappingJackson2JsonView);
    assertNotNull(mv.getModel());
    assertNotNull(mv.getModel().get(ApiExceptionResolver.MODEL_KEY));
    assertTrue(
        mv.getModel().get(ApiExceptionResolver.MODEL_KEY) instanceof RestApiException);

    RestApiException actual = (RestApiException) mv.getModel().get(ApiExceptionResolver.MODEL_KEY);

    assertNotNull(actual.getCause());
    assertEquals(cause.getMessage(), actual.getCause().getMessage());

    assertTrue(actual.getErrorCodeInherited());
    assertEquals(cause.getErrorCode(), actual.getErrorCode());

    assertNotNull(actual.getHandler());
    assertEquals(TestHandler.class.getName(), actual.getHandler().getClassName());
    assertEquals("testMethod", actual.getHandler().getMethodName());
    final List<String> expectedMethodParameters = new ArrayList<>(3);
    expectedMethodParameters.add(UUID.class.getName());
    expectedMethodParameters.add(String.class.getName());
    expectedMethodParameters.add(int.class.getName());
    assertEquals(expectedMethodParameters, actual.getHandler().getMethodParameterTypes());
    System.out.println(actual);
  }

  private static class TestHandler {

    /**
     * Test method string.
     *
     * @param uuid  the uuid
     * @param name  the name
     * @param value the value
     * @return the string
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    public String testMethod(UUID uuid, String name, int value) {
      return "test";
    }
  }
}
