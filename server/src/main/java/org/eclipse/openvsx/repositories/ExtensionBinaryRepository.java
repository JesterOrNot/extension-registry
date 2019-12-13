/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/
package org.eclipse.openvsx.repositories;

import org.springframework.data.repository.Repository;

import org.eclipse.openvsx.entities.ExtensionBinary;
import org.eclipse.openvsx.entities.ExtensionVersion;

public interface ExtensionBinaryRepository extends Repository<ExtensionBinary, Long> {

    ExtensionBinary findByExtension(ExtensionVersion extVersion);

}