package modula.parser.model;

import java.util.Map;

/**
 * NamespacePrefixesHolder接口
 */
public interface NamespacePrefixesHolder {

    /**
     * Get the map of namespaces, with keys as prefixes and values as URIs.
     *
     * @param namespaces The namespaces prefix map.
     */
    void setNamespaces(Map<String, String> namespaces);

    /**
     * Get the map of namespaces, with keys as prefixes and values as URIs.
     *
     * @return The namespaces prefix map.
     */
    Map<String, String> getNamespaces();

}