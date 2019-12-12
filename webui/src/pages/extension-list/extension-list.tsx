/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/

import * as React from "react";
import { Grid } from "@material-ui/core";
import { ExtensionListItem } from "./extension-list-item";
import { ExtensionFilter, ExtensionRaw } from "../../extension-registry-types";
import { ExtensionRegistryService } from "../../extension-registry-service";
import { debounce } from "../../utils";

export class ExtensionList extends React.Component<ExtensionList.Props, ExtensionList.State> {

    protected extensions: ExtensionRaw[];

    protected cancellationToken: { cancel?: () => void, timeout?: number } = {};

    constructor(props: ExtensionList.Props) {
        super(props);

        this.state = {
            extensions: []
        };
    }

    componentDidMount() {
        this.getExtensions(this.props.filter).then(extensions => this.setState({ extensions })).catch(() => {});
    }

    componentDidUpdate(prevProps: ExtensionList.Props, prevState: ExtensionList.State) {
        const prevFilter = prevProps.filter;
        const newFilter = this.props.filter;
        if (prevFilter.category !== newFilter.category || prevFilter.query !== newFilter.query) {
            if (this.cancellationToken.cancel) {
                this.cancellationToken.cancel();
                this.cancellationToken.cancel = undefined;
            }
            debounce(() => {
                this.props.service.getExtensions(newFilter).then(extensions => this.setState({ extensions }));
            }, this.cancellationToken);
        }
    }

    protected getExtensions(filter: ExtensionFilter) {
        return new Promise<ExtensionRaw[]>((resolve, reject) => {
            this.cancellationToken.cancel = reject;
            this.props.service.getExtensions(filter).then(ext => resolve(ext));
        });
    }

    render() {
        const extensionList = this.state.extensions.map((ext, idx) => {
            return <ExtensionListItem idx={idx} extension={ext} service={this.props.service} key={ext.name} />;
        });
        return <Grid container spacing={2}>
            {extensionList}
        </Grid>;
    }
}

export namespace ExtensionList {
    export interface Props {
        filter: ExtensionFilter,
        service: ExtensionRegistryService
    }
    export interface State {
        extensions: ExtensionRaw[]
    }
}

