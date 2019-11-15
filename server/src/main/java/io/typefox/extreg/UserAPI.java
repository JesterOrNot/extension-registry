/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/
package io.typefox.extreg;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.typefox.extreg.entities.UserData;
import io.typefox.extreg.entities.UserSession;
import io.typefox.extreg.json.UserJson;

@RestController
public class UserAPI {

    private static final String COOKIE_COMMENT = "User session id";
    private static final int COOKIE_MAX_AGE = 604_800; // one week in seconds

    @Autowired
    EntityManager entityManager;

    @Autowired
    EntityService entities;

    @Value("#{environment.REGISTRY_SERVER_URL}")
    String serverUrl;

    //XXX
    // @GetMapping(
    //     value = "/api/-/user",
    //     produces = MediaType.APPLICATION_JSON_VALUE
    // )
    // @Transactional
    // public Response userInfo(@CookieParam("sessionid") Cookie sessionCookie) {
    //     var json = new UserJson();
    //     if (sessionCookie == null) {
    //         json.error = "Not logged in.";
    //         return Response.ok(json).build();
    //     }
    //     var session = entities.findSession(sessionCookie.getValue());
    //     if (session == null) {
    //         json.error = "Invalid session.";
    //         return Response.ok(json).build();
    //     }
    //     updateLastUsed(session);
    //     json.name = session.getUser().getName();
    //     json.avatarUrl = "https://s.gravatar.com/avatar/9a638e5879d268e59d158a2091723c3c?s=80";
    //     return Response.ok(json)
    //             .cookie(new NewCookie(sessionCookie, COOKIE_COMMENT, COOKIE_MAX_AGE, false))
    //             .build();
    // }

    // @GetMapping("/api/-/user/login")
    // @Transactional
    // public Response login(@CookieValue("sessionid") String sessionCookie) {
    //     if (sessionCookie != null) {
    //         var session = entities.findSession(sessionCookie);
    //         if (session != null) {
    //             return Response.temporaryRedirect(getRedirectURI())
    //                 .cookie(new NewCookie(sessionCookie, COOKIE_COMMENT, COOKIE_MAX_AGE, false))
    //                 .build();
    //         }
    //     }
    //     var user = entityManager.find(UserData.class, 1l);
    //     var session = new UserSession();
    //     session.setId(UUID.randomUUID().toString());
    //     session.setUser(user);
    //     updateLastUsed(session);
    //     entityManager.persist(session);
    //     return Response.temporaryRedirect(getRedirectURI())
    //             .cookie(new NewCookie("sessionid", session.getId(),
    //                                   "/", getDomain(), COOKIE_COMMENT, COOKIE_MAX_AGE, false))
    //             .build();
    // }

    // @GetMapping("/api/-/user/logout")
    // @Transactional
    // public Response logout(@CookieValue("sessionid") String sessionCookie) {
    //     if (sessionCookie == null) {
    //         return Response.temporaryRedirect(getRedirectURI()).build();
    //     }
    //     var session = entities.findSession(sessionCookie);
    //     if (session != null) {
    //         entityManager.remove(session);
    //     }
    //     return Response.temporaryRedirect(getRedirectURI())
    //         .cookie(new NewCookie(sessionCookie, null, 0, false))
    //         .build();
    // }

    // private URI getRedirectURI() {
    //     try {
    //         return new URI("/");
    //     } catch (URISyntaxException exc) {
    //         throw new WebApplicationException(exc);
	// 	}
    // }

    // private String getDomain() {
    //     try {
    //         var uri = new URI(serverUrl);
    //         return uri.getHost();
    //     } catch (URISyntaxException exc) {
    //         throw new WebApplicationException(exc);
    //     }
    // }

    // private void updateLastUsed(UserSession session) {
    //     session.setLastUsed(LocalDateTime.now(ZoneId.of("UTC")));
    // }

}