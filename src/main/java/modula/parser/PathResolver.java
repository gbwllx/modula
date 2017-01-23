package modula.parser;

/**
 * 路径解析接口
 */
public interface PathResolver {

    /**
     * 解析ctxPath到绝对路径URL
     */
    String resolvePath(String ctxPath);

    /**
     * 获取以ctxPath为根的PathResolver
     */
    PathResolver getResolver(String ctxPath);

}

