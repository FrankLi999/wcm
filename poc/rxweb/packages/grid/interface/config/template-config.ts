import { ParameterConfig } from "./parameter-config";

export class TemplateConfig {
    div?: ElementConfig;
    span?: ElementConfig;
    table?: ElementConfig;
    thead?: ElementConfig;
    tr?: ElementConfig;
    th?: ElementConfig;
    tbody?: ElementConfig;
    td?: ElementConfig;
    text?: TextConfig;
    i?: ElementConfig;
    select?: ElementConfig;
    option?: ElementConfig;
    ul?: ElementConfig;
    li?: ElementConfig;
    a?: ElementConfig;
    label?: ElementConfig;
    img?: ElementConfig;
}

export class ElementConfig {
    style?: { [key: string]: any };
    class?: any[];
    attributes?: { [key: string]: any };
    text?: any[];
    childrens?: TemplateConfig[];
    sourceItems?: any;
    source?: Function;
    event?: { [key: string]: any }
    id?: string;
    parameterConfig?: ParameterConfig
}

export class TextConfig {
    text: string | Function;
}