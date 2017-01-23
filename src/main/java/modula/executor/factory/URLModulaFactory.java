package modula.executor.factory;

import modula.parser.model.ModelException;
import modula.parser.io.ModulaReader;
import modula.parser.model.Modula;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

/**
 * @description:
 * @author: gubing.gb
 * @date: 2017/1/19.
 */
public class URLModulaFactory extends AbstractModulaFactory {
    private final URL modulaURL;

    public URLModulaFactory(URL modulaURL) {
        this.modulaURL = modulaURL;
    }

    @Override
    public Modula createModula(Map<String, Class> actionMap) throws ModelException, XMLStreamException, IOException {
        Modula modula = ModulaReader.read(modulaURL, getConfiguration(actionMap));
        return modula;
    }
}
