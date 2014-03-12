/**
 * $Revision: 1722 $
 * $Date: 2005-07-28 15:19:16 -0700 (Thu, 28 Jul 2005) $
 *
 * Copyright (C) 2005-2008 Jive Software. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jivesoftware.openfire.plugin;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.muc.MUCRoom;
import org.jivesoftware.openfire.muc.MultiUserChatManager;
import org.jivesoftware.openfire.muc.MultiUserChatService;
import org.jivesoftware.openfire.muc.NotAllowedException;
import org.jivesoftware.util.AlreadyExistsException;
import org.jivesoftware.util.NotFoundException;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.PropertyEventDispatcher;
import org.jivesoftware.util.PropertyEventListener;
import org.jivesoftware.util.StringUtils;
import org.xmpp.packet.JID;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Plugin that allows the administration of users via HTTP requests.
 *
 * @author Justin Hunt
 */
public class RoomServicePlugin implements Plugin, PropertyEventListener {
    private MultiUserChatManager chatManager;
    private XMPPServer server;

    private String secret;
    private boolean enabled;
    private Collection<String> allowedIPs;

    public void initializePlugin(PluginManager manager, File pluginDirectory) {
        server = XMPPServer.getInstance();
        chatManager = server.getMultiUserChatManager();

        secret = JiveGlobals.getProperty("plugin.roomservice.secret", "");
        // If no secret key has been assigned to the user service yet, assign a random one.
        if (secret.equals("")){
            secret = StringUtils.randomString(8);
            setSecret(secret);
        }
        
        // See if the service is enabled or not.
        enabled = JiveGlobals.getBooleanProperty("plugin.roomservice.enabled", false);

        // Get the list of IP addresses that can use this service. An empty list means that this filter is disabled.
        allowedIPs = StringUtils.stringToCollection(JiveGlobals.getProperty("plugin.roomservice.allowedIPs", ""));

        // Listen to system property events
        PropertyEventDispatcher.addListener(this);
    }

    public void destroyPlugin() {
        chatManager = null;
        // Stop listening to system property events
        PropertyEventDispatcher.removeListener(this);
    }

    public void createChat(String jidNode, String subdomain, String roomName) throws NotAllowedException {
        MultiUserChatService multiUserChatService = chatManager.getMultiUserChatService(subdomain);
        //JID address = new JID(jidNode,jidDomain,jidResource);
        JID address = new JID(jidNode);
        multiUserChatService.getChatRoom(roomName, address);
    }

    public void createChat2(String jidNode, String jidDomain,
                           String jidResource  , String subdomain, String roomName, String roomNaturalName,String roomSubject, String roomDescription, String maxUsers) throws NotAllowedException {
        //room = webManager.getMultiUserChatManager().getMultiUserChatService(roomJID).getChatRoom(roomName, address);
        MultiUserChatService multiUserChatService = chatManager.getMultiUserChatService(subdomain);
        JID address = new JID(jidNode,jidDomain,jidResource);
        //JID address = server.createJID("AutoRoomCreator",jidResource);
        multiUserChatService.getChatRoom(roomName, address);
        MUCRoom room = multiUserChatService.getChatRoom(roomName);
        //maxUsers = "10";
        room.setMaxUsers(Integer.parseInt(maxUsers));
        //broadcastModerator = "true";
        //room.setModerated(true);
        room.setPublicRoom(true);
        room.setDescription(roomDescription);
        room.setNaturalLanguageName(roomNaturalName);
        room.setSubject(roomSubject);
        //publicRoom = "true";
        // Rooms created from the admin console are always persistent
        //persistentRoom = "true";
        room.setPersistent(true);



        room.saveToDB();

    }
    
    public void deleteChat(String jid, String subdomain, String roomName) {
        MultiUserChatService multiUserChatService = chatManager.getMultiUserChatService(subdomain);
        multiUserChatService.removeChatRoom(roomName);
    }

    //http://www.igniterealtime.org/builds/openfire/docs/latest/documentation/javadoc/org/jivesoftware/openfire/muc/MultiUserChatManager.html#createMultiUserChatService%28java.lang.String,%20java.lang.String,%20java.lang.Boolean%29
    public MultiUserChatService createMultiUserChatService(String subdomain,
                                                               String description,
                                                               Boolean isHidden){
																   try{
        MultiUserChatService multiUserChatService = chatManager.createMultiUserChatService(subdomain, description, isHidden);
        return multiUserChatService;
}
		catch (AlreadyExistsException e) {
         throw new RuntimeException("AlreadyExistsException");
        }
        
        //    throws AlreadyExistsException
    }

    public void updateMultiUserChatService(String cursubdomain, String newsubdomain, String description)
    {
		try{
        chatManager.updateMultiUserChatService(cursubdomain, newsubdomain, description);
        }
		catch (NotFoundException e) {
         throw new RuntimeException("NotFoundException");
        }
        //throws NotFoundException
    }
    public void removeMultiUserChatService(String subdomain)
    {
		try
		{
         chatManager.removeMultiUserChatService(subdomain);
         }
		catch (NotFoundException e) {
         throw new RuntimeException("NotFoundException");
        }
       //throws NotFoundException
    }

    /**
     * Returns the secret key that only valid requests should know.
     *
     * @return the secret key.
     */
    public String getSecret() {
        return secret;
    }

    /**
     * Sets the secret key that grants permission to use the userservice.
     *
     * @param secret the secret key.
     */
    public void setSecret(String secret) {
        JiveGlobals.setProperty("plugin.roomservice.secret", secret);
        this.secret = secret;
    }

    public Collection<String> getAllowedIPs() {
        return allowedIPs;
    }

    public void setAllowedIPs(Collection<String> allowedIPs) {
        JiveGlobals.setProperty("plugin.roomservice.allowedIPs", StringUtils.collectionToString(allowedIPs));
        this.allowedIPs = allowedIPs;
    }

    /**
     * Returns true if the user service is enabled. If not enabled, it will not accept
     * requests to create new accounts.
     *
     * @return true if the user service is enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Enables or disables the user service. If not enabled, it will not accept
     * requests to create new accounts.
     *
     * @param enabled true if the user service should be enabled.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        JiveGlobals.setProperty("plugin.roomservice.enabled",  enabled ? "true" : "false");
    }

    public void propertySet(String property, Map<String, Object> params) {
        if (property.equals("plugin.roomservice.secret")) {
            this.secret = (String)params.get("value");
        }
        else if (property.equals("plugin.roomservice.enabled")) {
            this.enabled = Boolean.parseBoolean((String)params.get("value"));
        }
        else if (property.equals("plugin.roomservice.allowedIPs")) {
            this.allowedIPs = StringUtils.stringToCollection((String)params.get("value"));
        }
    }

    public void propertyDeleted(String property, Map<String, Object> params) {
        if (property.equals("plugin.roomservice.secret")) {
            this.secret = "";
        }
        else if (property.equals("plugin.roomservice.enabled")) {
            this.enabled = false;
        }
        else if (property.equals("plugin.roomservice.allowedIPs")) {
            this.allowedIPs = Collections.emptyList();
        }
    }

    public void xmlPropertySet(String property, Map<String, Object> params) {
        // Do nothing
    }

    public void xmlPropertyDeleted(String property, Map<String, Object> params) {
        // Do nothing
    }
}
