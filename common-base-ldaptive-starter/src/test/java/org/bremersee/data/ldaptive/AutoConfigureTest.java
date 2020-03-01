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

package org.bremersee.data.ldaptive;

import static org.junit.Assert.assertTrue;

import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.data.ldaptive.app.GroupMapper;
import org.bremersee.data.ldaptive.app.PersonMapper;
import org.bremersee.data.ldaptive.app.TestConfiguration;
import org.junit.jupiter.api.Test;
import org.ldaptive.LdapEntry;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

/**
 * The auto configure test.
 *
 * @author Christian Bremer
 */
@SpringBootTest(
    classes = TestConfiguration.class,
    webEnvironment = WebEnvironment.NONE,
    properties = {
        "spring.ldap.embedded.base-dn=dc=bremersee,dc=org",
        "spring.ldap.embedded.credential.username=uid=admin",
        "spring.ldap.embedded.credential.password=secret",
        "spring.ldap.embedded.ldif=classpath:schema.ldif",
        "spring.ldap.embedded.port=12389",
        "spring.ldap.embedded.validation.enabled=false",
        "bremersee.ldaptive.enabled=true",
        "bremersee.ldaptive.use-unbound-id-provider=true",
        "bremersee.ldaptive.ldap-url=ldap://localhost:12389",
        "bremersee.ldaptive.use-ssl=false",
        "bremersee.ldaptive.use-start-tls=false",
        "bremersee.ldaptive.bind-dn=uid=admin",
        "bremersee.ldaptive.bind-credential=secret",
        "bremersee.ldaptive.pooled=false"
    })
@Slf4j
class AutoConfigureTest {

  @Value("${spring.ldap.embedded.base-dn}")
  private String baseDn;

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  private LdaptiveTemplate ldaptiveTemplate;

  @Autowired
  private GroupMapper groupMapper;

  @Autowired
  private PersonMapper personMapper;

  /**
   * Find existing persons.
   */
  @Test
  void findExistingPersons() {
    SearchFilter searchFilter = new SearchFilter("(objectclass=inetOrgPerson)");
    SearchRequest searchRequest = new SearchRequest(
        "ou=people," + baseDn, searchFilter);
    searchRequest.setSearchScope(SearchScope.ONELEVEL);

    // without mapper
    Collection<LdapEntry> entries = ldaptiveTemplate.findAll(searchRequest);
    entries.forEach(ldapEntry -> log.info("Ldap entry found with cn = {}",
        ldapEntry.getAttribute("cn").getStringValue()));
    assertTrue(entries.stream()
        .anyMatch(entry -> "Anna Livia Plurabelle"
            .equalsIgnoreCase(entry.getAttribute("cn").getStringValue())));
    assertTrue(entries.stream()
        .anyMatch(entry -> "Gustav Anias Horn"
            .equalsIgnoreCase(entry.getAttribute("cn").getStringValue())));
    assertTrue(entries.stream()
        .anyMatch(entry -> "Hans Castorp"
            .equalsIgnoreCase(entry.getAttribute("cn").getStringValue())));

    // with mapper
    assertTrue(ldaptiveTemplate.findAll(searchRequest, personMapper)
        .anyMatch(entry -> "Anna Livia Plurabelle"
            .equalsIgnoreCase(entry.getCn())));
    assertTrue(ldaptiveTemplate.findAll(searchRequest, personMapper)
        .anyMatch(entry -> "Gustav Anias Horn"
            .equalsIgnoreCase(entry.getCn())));
    assertTrue(ldaptiveTemplate.findAll(searchRequest, personMapper)
        .anyMatch(entry -> "Hans Castorp"
            .equalsIgnoreCase(entry.getCn())));
  }

  /**
   * Find existing groups.
   */
  @Test
  void findExistingGroups() {
    SearchFilter searchFilter = new SearchFilter("(objectclass=groupOfUniqueNames)");
    SearchRequest searchRequest = new SearchRequest(
        "ou=groups," + baseDn, searchFilter);
    searchRequest.setSearchScope(SearchScope.ONELEVEL);

    // without mapper
    Collection<LdapEntry> entries = ldaptiveTemplate.findAll(searchRequest);
    entries.forEach(ldapEntry -> log.info("Ldap entry found with cn = {}",
        ldapEntry.getAttribute("cn").getStringValue()));
    assertTrue(entries.stream()
        .anyMatch(entry -> "developers"
            .equalsIgnoreCase(entry.getAttribute("cn").getStringValue())));
    assertTrue(entries.stream()
        .anyMatch(entry -> "managers"
            .equalsIgnoreCase(entry.getAttribute("cn").getStringValue())));

    // with mapper
    assertTrue(ldaptiveTemplate.findAll(searchRequest, groupMapper)
        .anyMatch(entry -> "developers"
            .equalsIgnoreCase(entry.getCn())));
    assertTrue(ldaptiveTemplate.findAll(searchRequest, groupMapper)
        .anyMatch(entry -> "managers"
            .equalsIgnoreCase(entry.getCn())));
  }

}
