/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/
package io.typefox.extreg;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.hibernate.Hibernate;
import org.hibernate.engine.spi.SessionImplementor;

import io.typefox.extreg.entities.Extension;
import io.typefox.extreg.entities.ExtensionVersion;
import io.typefox.extreg.entities.Publisher;
import io.typefox.extreg.json.ExtensionInfo;
import io.typefox.extreg.json.ExtensionReference;
import io.typefox.extreg.json.PublisherInfo;
import io.typefox.extreg.json.SearchResult;
import io.typefox.extreg.util.ErrorResultException;

@Path("/api")
public class RegistryAPI {

    @Inject
    private EntityManager entityManager;

    @GET
    @Path("/{publisher}")
    @Produces(MediaType.APPLICATION_JSON)
    public PublisherInfo getPublisher(@PathParam("publisher") String publisherName) {
        try {
            var publisher = findPublisher(publisherName);
            var json = new PublisherInfo();
            json.name = publisher.getName();
            json.extensions = findExtensionNames(publisher);
            return json;
        } catch (NoResultException exc) {
            throw new NotFoundException(exc);
        }
    }

    @GET
    @Path("/{publisher}/{extension}")
    @Produces(MediaType.APPLICATION_JSON)
    public ExtensionInfo getExtension(@PathParam("publisher") String publisherName,
            @PathParam("extension") String extensionName) {
        try {
            var publisher = findPublisher(publisherName);
            var extension = findExtension(extensionName, publisher);
            var json = new ExtensionInfo();
            copyMetadata(json, extension.getLatest());
            return json;
        } catch (NoResultException exc) {
            throw new NotFoundException(exc);
        }
    }

    @GET
    @Path("/{publisher}/{extension}/{version}")
    @Produces(MediaType.APPLICATION_JSON)
    public ExtensionInfo getExtensionVersion(@PathParam("publisher") String publisherName,
            @PathParam("extension") String extensionName, @PathParam("version") String version) {
        try {
            var publisher = findPublisher(publisherName);
            var extension = findExtension(extensionName, publisher);
            var extVersion = findVersion(version, extension);
            var json = new ExtensionInfo();
            copyMetadata(json, extVersion);
            return json;
        } catch (NoResultException exc) {
            throw new NotFoundException(exc);
        }
    }

    @GET
    @Path("/-/search")
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResult search(@QueryParam("query") String query) {
        var json = new SearchResult();
        // TODO
        return json;
    }

    @POST
    @Path("/-/publish")
    @Transactional
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_JSON)
    public ExtensionInfo publish(byte[] content) {
        try {
            var processor = new ExtensionProcessor(content);
            var publisher = findPublisher(processor.getPublisherName());
            var extension = findExtension(processor.getExtensionName(), publisher);
            var extVersion = processor.getMetadata();
            checkUniqueVersion(extVersion.getVersion(), extension);
            processor.getExtensionDependencies().forEach(dep -> addDependency(dep, extVersion));
            processor.getBundledExtensions().forEach(dep -> addBundledExtension(dep, extVersion));

            var lobCreator = Hibernate.getLobCreator((SessionImplementor) entityManager);
            entityManager.persist(extVersion);
            var binary = processor.getBinary(extVersion, lobCreator);
            entityManager.persist(binary);
            var readme = processor.getReadme(extVersion, lobCreator);
            if (readme != null)
                entityManager.persist(readme);
            var icon = processor.getIcon(extVersion, lobCreator);
            if (icon != null)
                entityManager.persist(icon);

            var json = new ExtensionInfo();
            copyMetadata(json, extVersion);
            return json;
        } catch (ErrorResultException | NoResultException exc) {
            return ExtensionInfo.error(exc.getMessage());
        }
    }

    private Publisher findPublisher(String name) {
        var qs = "SELECT pub FROM Publisher pub WHERE (pub.name = :name)";
        var query = entityManager.createQuery(qs, Publisher.class);
        query.setParameter("name", name);
        return query.getSingleResult();
    }

    private Extension findExtension(String name, Publisher publisher) {
        var qs = "SELECT ext FROM Extension ext WHERE (ext.publisher = :publisher and ext.name = :name)";
        var query = entityManager.createQuery(qs, Extension.class);
        query.setParameter("publisher", publisher.getId());
        query.setParameter("name", name);
        return query.getSingleResult();
    }

    private List<String> findExtensionNames(Publisher publisher) {
        var qs = "SELECT ext.name FROM Extension ext WHERE (ext.publisher = :publisher)";
        var query = entityManager.createQuery(qs, String.class);
        query.setParameter("publisher", publisher.getId());
        return query.getResultList();
    }

    private ExtensionVersion findVersion(String version, Extension extension) {
        var qs = "SELECT exv FROM ExtensionVersion exv WHERE (exv.extension = :extension and exv.version = :version)";
        var query = entityManager.createQuery(qs, ExtensionVersion.class);
        query.setParameter("extension", extension.getId());
        query.setParameter("version", version);
        return query.getSingleResult();
    }

    private List<String> findAllVersions(Extension extension) {
        var qs = "SELECT exv.version FROM ExtensionVersion exv WHERE (exv.extension = :extension)";
        var query = entityManager.createQuery(qs, String.class);
        query.setParameter("extension", extension.getId());
        return query.getResultList();
    }

    private void checkUniqueVersion(String version, Extension extension) {
        var qs = "SELECT count(*) FROM ExtensionVersion exv WHERE (exv.extension = :extension and exv.version = :version)";
        var query = entityManager.createQuery(qs, Long.class);
        query.setParameter("extension", extension.getId());
        query.setParameter("version", version);
        if (query.getSingleResult() > 0)
            throw new ErrorResultException("Extension " + extension.getName() + " version " + version + " is already published.");
    }

    private void copyMetadata(ExtensionInfo json, ExtensionVersion extVersion) {
        var extension = extVersion.getExtension();
        json.publisher = extension.getPublisher().getName();
        json.name = extension.getName();
        json.allVersions = findAllVersions(extension);
        json.version = extVersion.getVersion();
        json.preview = extVersion.isPreview();
        json.timestamp = extVersion.getTimestamp();
        json.displayName = extVersion.getDisplayName();
        json.description = extVersion.getDescription();
        json.categories = extVersion.getCategories();
        json.keywords = extVersion.getKeywords();
        json.license = extVersion.getLicense();
        json.homepage = extVersion.getHomepage();
        json.repository = extVersion.getRepository();
        json.bugs = extVersion.getBugs();
        json.markdown = extVersion.getMarkdown();
        json.galleryColor = extVersion.getGalleryColor();
        json.galleryTheme = extVersion.getGalleryTheme();
        json.qna = extVersion.getQna();
        if (extVersion.getDependencies() != null) {
            json.dependencies = new ArrayList<>();
            for (Extension depExtension : extVersion.getDependencies()) {
                var ref = new ExtensionReference();
                ref.publisher = depExtension.getPublisher().getName();
                ref.extension = depExtension.getName();
                json.dependencies.add(ref);
            }
        }
        if (extVersion.getBundledExtensions() != null) {
            json.bundledExtensions = new ArrayList<>();
            for (Extension bndExtension : extVersion.getBundledExtensions()) {
                var ref = new ExtensionReference();
                ref.publisher = bndExtension.getPublisher().getName();
                ref.extension = bndExtension.getName();
                json.bundledExtensions.add(ref);
            }
        }
    }

    private void addDependency(String dependency, ExtensionVersion extVersion) {
        var split = dependency.split("\\.");
        if (split.length != 2)
            return;
        try {
            var publisher = findPublisher(split[0]);
            var extension = findExtension(split[1], publisher);
            var depList = extVersion.getDependencies();
            if (depList == null) {
                depList = new ArrayList<Extension>();
                extVersion.setDependencies(depList);
            }
            depList.add(extension);
        } catch (NoResultException exc) {
            // Ignore the entry
        }
    }

    private void addBundledExtension(String bundled, ExtensionVersion extVersion) {
        var split = bundled.split("\\.");
        if (split.length != 2)
            return;
        try {
            var publisher = findPublisher(split[0]);
            var extension = findExtension(split[1], publisher);
            var depList = extVersion.getBundledExtensions();
            if (depList == null) {
                depList = new ArrayList<Extension>();
                extVersion.setBundledExtensions(depList);
            }
            depList.add(extension);
        } catch (NoResultException exc) {
            // Ignore the entry
        }
    }

}