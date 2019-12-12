/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/
package io.typefox.extreg.repositories;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Component;

import io.typefox.extreg.entities.Extension;
import io.typefox.extreg.entities.ExtensionBinary;
import io.typefox.extreg.entities.ExtensionIcon;
import io.typefox.extreg.entities.ExtensionReadme;
import io.typefox.extreg.entities.ExtensionReview;
import io.typefox.extreg.entities.ExtensionVersion;
import io.typefox.extreg.entities.Publisher;
import io.typefox.extreg.entities.UserSession;

@Component
public class RepositoryService {

    @Autowired PublisherRepository publisherRepo;
    @Autowired ExtensionRepository extensionRepo;
    @Autowired ExtensionVersionRepository extensionVersionRepo;
    @Autowired ExtensionBinaryRepository extensionBinaryRepo;
    @Autowired ExtensionIconRepository extensionIconRepo;
    @Autowired ExtensionReadmeRepository extensionReadmeRepo;
    @Autowired ExtensionReviewRepository extensionReviewRepo;
    @Autowired UserSessionRepository userSessionRepo;

    public Publisher findPublisher(String name) {
        return publisherRepo.findByName(name);
    }

    public Extension findExtension(String name, Publisher publisher) {
        return extensionRepo.findByNameAndPublisher(name, publisher);
    }

    public Extension findExtension(String name, String publisherName) {
        return extensionRepo.findByNameAndPublisherName(name, publisherName);
    }

    public List<Extension> findExtensions(Publisher publisher) {
        return extensionRepo.findByPublisherOrderByNameAsc(publisher);
    }

    public Streamable<Extension> findAllExtensions() {
        return extensionRepo.findAll();
    }

    public ExtensionVersion findVersion(String version, Extension extension) {
        return extensionVersionRepo.findByVersionAndExtension(version, extension);
    }

    public ExtensionVersion findVersion(String version, String extensionName, String publisherName) {
        return extensionVersionRepo.findByVersionAndExtensionNameAndExtensionPublisherName(version, extensionName, publisherName);
    }

    public List<ExtensionVersion> findVersions(Extension extension) {
        return extensionVersionRepo.findByExtension(extension);
    }

    public ExtensionBinary findBinary(ExtensionVersion extVersion) {
        return extensionBinaryRepo.findByExtension(extVersion);
    }

    public ExtensionIcon findIcon(ExtensionVersion extVersion) {
        return extensionIconRepo.findByExtension(extVersion);
    }

    public ExtensionReadme findReadme(ExtensionVersion extVersion) {
        return extensionReadmeRepo.findByExtension(extVersion);
    }

    public List<ExtensionReview> findReviews(Extension extension) {
        return extensionReviewRepo.findByExtension(extension);
    }

    public long countReviews(Extension extension) {
        return extensionReviewRepo.countByExtension(extension);
    }

    public UserSession findUserSession(String id) {
        return userSessionRepo.findById(id);
    }

}