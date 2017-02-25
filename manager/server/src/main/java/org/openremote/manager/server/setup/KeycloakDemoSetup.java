/*
 * Copyright 2016, OpenRemote Inc.
 *
 * See the CONTRIBUTORS.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.openremote.manager.server.setup;

import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.openremote.container.Container;
import org.openremote.manager.shared.security.ClientRole;
import org.openremote.manager.shared.security.Tenant;

import java.util.Arrays;
import java.util.logging.Logger;

/**
 * We have three demo users:
 * <ul>
 *     <li><code>admin</code> - The superuser in the "master" realm with all access</li>
 *     <li><code>testuser1</code> - (Password: testuser1) A user in the "master" realm with read/write access to assets</li>
 *     <li><code>testuser2</code> - (Password: testuser2) A user in the "customerA" realm with only read access to assets</li>
 * </ul>
 */
public class KeycloakDemoSetup extends AbstractKeycloakSetup {

    private static final Logger LOG = Logger.getLogger(KeycloakDemoSetup.class.getName());

    public KeycloakDemoSetup(Container container) {
        super(container);
    }

    @Override
    public void execute() throws Exception {
        String masterClientObjectId = getClientObjectId(masterClientsResource);
        RolesResource masterRolesResource = masterClientsResource.get(masterClientObjectId).roles();

        // Create the 'test1' user in master realm
        UserRepresentation testuser1 = new UserRepresentation();
        testuser1.setUsername("testuser1");
        testuser1.setFirstName("Testuserfirst");
        testuser1.setLastName("Testuserlast");
        testuser1.setEnabled(true);
        masterUsersResource.create(testuser1);
        testuser1 = masterUsersResource.search("testuser1", null, null, null, null, null).get(0);
        CredentialRepresentation testuser1Credentials = new CredentialRepresentation();
        testuser1Credentials.setType("password");
        testuser1Credentials.setValue("testuser1");
        testuser1Credentials.setTemporary(false);
        masterUsersResource.get(testuser1.getId()).resetPassword(testuser1Credentials);
        masterUsersResource.get(testuser1.getId()).roles().clientLevel(masterClientObjectId).add(Arrays.asList(
            masterRolesResource.get(ClientRole.READ_ASSETS.getValue()).toRepresentation(),
            masterRolesResource.get(ClientRole.WRITE_ASSETS.getValue()).toRepresentation()
        ));
        LOG.info("Added demo user '" + testuser1.getUsername() + "' with password '" + testuser1Credentials.getValue() + "'");

        // Create additional test realms/tenants
        Tenant customerA = new Tenant();
        customerA.setRealm("customerA");
        customerA.setDisplayName("Customer A");
        customerA.setEnabled(true);
        identityService.createTenant(accessToken, customerA);

        ClientsResource customerAClientsResource = identityService.getRealms(accessToken, false).realm("customerA").clients();
        String customerAClientObjectId = getClientObjectId(customerAClientsResource);
        RolesResource customerARolesResource = customerAClientsResource.get(customerAClientObjectId).roles();

        UserRepresentation testuser2 = new UserRepresentation();
        testuser2.setUsername("testuser2");
        testuser2.setFirstName("Testuserfirst");
        testuser2.setLastName("Testuserlast");
        testuser2.setEnabled(true);
        UsersResource customerAUsersResource = identityService.getRealms(accessToken, false).realm("customerA").users();
        customerAUsersResource.create(testuser2);
        testuser2 = customerAUsersResource.search("test", null, null, null, null, null).get(0);
        CredentialRepresentation testuser2Credentials = new CredentialRepresentation();
        testuser2Credentials.setType("password");
        testuser2Credentials.setValue("testuser2");
        testuser2Credentials.setTemporary(false);
        customerAUsersResource.get(testuser2.getId()).resetPassword(testuser2Credentials);
        customerAUsersResource.get(testuser2.getId()).roles().clientLevel(customerAClientObjectId).add(Arrays.asList(
            customerARolesResource.get(ClientRole.READ_ASSETS.getValue()).toRepresentation()
        ));
        LOG.info("Added demo user '" + testuser1.getUsername() + "' with password '" + testuser2Credentials.getValue() + "'");

        Tenant customerB = new Tenant();
        customerB.setRealm("customerB");
        customerB.setDisplayName("Customer B");
        customerB.setEnabled(true);
        identityService.createTenant(accessToken, customerB);
    }
}
