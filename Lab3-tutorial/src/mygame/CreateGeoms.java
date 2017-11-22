package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

/**
 * This class creates the scene graph we make use of in this demo.
 *
 * The main feature is the method "toggleColor" that toggles the color of the
 * box in the scene between two given colors.
 *
 * @author hj
 */
public class CreateGeoms extends Node {

    private ColorRGBA current, next;
    private final Material mat;

    /**
     * This method creates a scene containing a box with color firstColor. The
     * box also has another color secondColor, and whenever the method
     * toogleColor is called the color of the box changes between the two.
     *
     * @param simpApp the SimpleApplication to which the box will belong
     * @param firstColor On of the colors the box will have.
     * @param secondColor The other color the box can have.
     */
    public CreateGeoms(SimpleApplication simpApp, ColorRGBA firstColor,
            ColorRGBA secondColor) {
        Geometry geom = new Geometry("Box", new Box(1, 1, 1));
        mat = new Material(simpApp.getAssetManager(),
                "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", firstColor);
        geom.setMaterial(mat);
        this.attachChild(geom);
        this.current = firstColor;
        this.next = secondColor;
    }

    /**
     * Toggles color of the box.
     */
    public void toggleColor() {
        ColorRGBA tmp; // used in the swap below
        // box has color this.current
        mat.setColor("Color", next); // change color to this.next 
        tmp = next; // swap colors so the other color will be used
        next = current; // the next time the method is called
        current = tmp;
    }
}