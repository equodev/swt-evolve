package org.eclipse.swt.custom;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stand-ins for the e4 application model, which {@link ViewRails} reads by reflection and matches by
 * interface name: each implements an interface with the e4 one's simple name. Built by hand, or
 * loaded from a {@code workbench.xmi} e4 saved.
 */
public final class E4ModelFixture {

    public interface MUIElement {}
    public interface MElementContainer extends MUIElement {}
    public interface MPartSashContainer extends MElementContainer {}
    public interface MPartStack extends MElementContainer {}
    public interface MArea extends MPartSashContainer {}
    public interface MPlaceholder extends MUIElement {}
    public interface MPerspectiveStack extends MElementContainer {}
    public interface MPerspective extends MElementContainer {}
    public interface MTrimmedWindow extends MElementContainer {}

    public static class UiElement implements MUIElement {
        public String elementId;
        public String containerData;
        public boolean toBeRendered = true;
        public boolean visible = true;
        public boolean horizontal;
        public final List<String> tags = new ArrayList<>();
        public final List<Object> children = new ArrayList<>();
        public Object selectedElement;
        public Object ref;

        public String getElementId() { return elementId; }
        public String getContainerData() { return containerData; }
        public boolean isToBeRendered() { return toBeRendered; }
        public boolean isVisible() { return visible; }
        public List<String> getTags() { return tags; }

        public UiElement data(String containerData) {
            this.containerData = containerData;
            return this;
        }

        public UiElement tag(String tag) {
            tags.add(tag);
            return this;
        }

        public UiElement unrendered() {
            toBeRendered = false;
            return this;
        }
    }

    public static class Container extends UiElement implements MElementContainer {
        public List<Object> getChildren() { return children; }
        public Object getSelectedElement() { return selectedElement; }
    }

    public static class Sash extends Container implements MPartSashContainer {
        public boolean isHorizontal() { return horizontal; }
    }

    public static class Stack extends Container implements MPartStack {}

    public static class Area extends Sash implements MArea {}

    public static class Placeholder extends UiElement implements MPlaceholder {
        public Object getRef() { return ref; }
    }

    public static class PerspectiveStack extends Container implements MPerspectiveStack {}

    public static class Perspective extends Container implements MPerspective {}

    public static class Window extends Container implements MTrimmedWindow {}

    public static Sash sash(String data, boolean horizontal, UiElement... children) {
        Sash sash = new Sash();
        sash.containerData = data;
        sash.horizontal = horizontal;
        sash.children.addAll(Arrays.asList(children));
        return sash;
    }

    public static Stack stack(String id, String data) {
        Stack stack = new Stack();
        stack.elementId = id;
        stack.containerData = data;
        return stack;
    }

    public static Area area(String data) {
        Area area = new Area();
        area.containerData = data;
        return area;
    }

    /** A loaded model: its window, and every element by element id. */
    public static final class Loaded {
        public final Window window;
        public final Map<String, UiElement> byId;

        Loaded(Window window, Map<String, UiElement> byId) {
            this.window = window;
            this.byId = byId;
        }

        public UiElement get(String elementId) {
            UiElement element = byId.get(elementId);
            if (element == null) throw new IllegalArgumentException("no element " + elementId);
            return element;
        }
    }

    /** The first window of the {@code workbench.xmi} at {@code resource}, beside this class. */
    public static Loaded load(String resource) throws Exception {
        Element root;
        try (InputStream in = E4ModelFixture.class.getResourceAsStream(resource)) {
            if (in == null) throw new IllegalArgumentException("no resource " + resource);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            root = factory.newDocumentBuilder().parse(in).getDocumentElement();
        }
        Map<String, UiElement> byXmiId = new HashMap<>();
        Map<String, UiElement> byId = new HashMap<>();
        Map<UiElement, Element> source = new HashMap<>();
        Window window = null;
        for (Element child : elements(root, "children")) {
            UiElement element = build(child, byXmiId, byId, source);
            if (element instanceof Window) {
                window = (Window) element;
                break;
            }
        }
        if (window == null) throw new IllegalArgumentException(resource + " has no window");
        for (Map.Entry<UiElement, Element> entry : source.entrySet()) {
            entry.getKey().selectedElement = byXmiId.get(entry.getValue().getAttribute("selectedElement"));
            entry.getKey().ref = byXmiId.get(entry.getValue().getAttribute("ref"));
        }
        return new Loaded(window, byId);
    }

    private static UiElement build(Element xml, Map<String, UiElement> byXmiId, Map<String, UiElement> byId,
                                   Map<UiElement, Element> source) {
        UiElement element = forType(xml.getAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "type"));
        element.elementId = attribute(xml, "elementId");
        element.containerData = attribute(xml, "containerData");
        element.horizontal = "true".equals(xml.getAttribute("horizontal"));
        element.toBeRendered = !"false".equals(xml.getAttribute("toBeRendered"));
        element.visible = !"false".equals(xml.getAttribute("visible"));
        for (Element tag : elements(xml, "tags")) element.tags.add(tag.getTextContent());
        byXmiId.put(xml.getAttributeNS("http://www.omg.org/XMI", "id"), element);
        if (element.elementId != null) byId.putIfAbsent(element.elementId, element);
        source.put(element, xml);
        for (Element child : elements(xml, "children")) element.children.add(build(child, byXmiId, byId, source));
        // The window's shared elements (the editor area) are not its children: a placeholder names them.
        for (Element shared : elements(xml, "sharedElements")) build(shared, byXmiId, byId, source);
        return element;
    }

    private static UiElement forType(String type) {
        switch (type) {
            case "basic:TrimmedWindow": return new Window();
            case "basic:PartSashContainer": return new Sash();
            case "basic:PartStack": return new Stack();
            case "advanced:Area": return new Area();
            case "advanced:Placeholder": return new Placeholder();
            case "advanced:PerspectiveStack": return new PerspectiveStack();
            case "advanced:Perspective": return new Perspective();
            default: throw new IllegalArgumentException("unexpected model type " + type);
        }
    }

    private static String attribute(Element xml, String name) {
        return xml.hasAttribute(name) ? xml.getAttribute(name) : null;
    }

    private static List<Element> elements(Element parent, String name) {
        List<Element> found = new ArrayList<>();
        for (Node n = parent.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n instanceof Element && name.equals(n.getLocalName())) found.add((Element) n);
        }
        return found;
    }

    private E4ModelFixture() {
    }
}
