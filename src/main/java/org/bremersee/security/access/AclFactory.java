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

package org.bremersee.security.access;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;
import org.bremersee.common.model.AccessControlEntry;
import org.bremersee.common.model.AccessControlList;

/**
 * @author Christian Bremer
 */
public interface AclFactory<T> {

  T createAccessControlList(String owner, Map<String, ? extends Ace> entries);

  static AclFactory<AccessControlList> dtoFactory() {
    return new DtoAclFactory();
  }

  class DtoAclFactory implements AclFactory<AccessControlList> {

    @Override
    public AccessControlList createAccessControlList(
        final String owner,
        final Map<String, ? extends Ace> entries) {
      final AccessControlList acl = new AccessControlList();
      acl.setOwner(owner);
      if (entries != null) {
        acl.setEntries(entries
            .entrySet()
            .stream()
            .map(entry -> {
              final String permission = entry.getKey();
              final Ace ace = entry.getValue();
              return new AccessControlEntry()
                  .permission(permission)
                  .guest(ace.isGuest())
                  .users(new ArrayList<>(ace.getUsers()))
                  .roles(new ArrayList<>(ace.getRoles()))
                  .groups(new ArrayList<>(ace.getGroups()));
            })
            .collect(Collectors.toList()));
      }
      return acl;
    }
  }

}
