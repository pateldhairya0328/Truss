package truss;

import java.util.ArrayList;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Joint extends Circle{
    double uX = 0, uY = 0;
    double angle = 90;
    ArrayList<Beam> attachedBeams = new ArrayList<Beam>(0);
    final int VAL = 0, DIR = 1;
    int type = 4;
    String types[] = {"Roller Support", "Pin Support", "Fixed Support", "Joint", "Error"};
    Color colors[] = {Color.DEEPSKYBLUE, Color.CRIMSON, Color.FORESTGREEN, Color.DIMGRAY};
    ArrayList<Double> forcevals = new ArrayList<>();
    ArrayList<Double> forcedirs = new ArrayList<>();
    ArrayList<Arrow> arrows = new ArrayList<>();
    String name = "";
    
    public Joint(double uX, double uY, int type, String name){
        super(uX*(App.pStep/App.uStep)+App.pOffsetX+App.pWidth/2, -uY*(App.pStep/App.uStep)+App.pOffsetY+App.pHeight/2, 0.2*(App.pStep/App.uStep));
        if (this.getRadius()<4){
            this.setRadius(4);
        }        
        this.setFill(colors[type]);
        this.type = type;
        this.uX = uX;
        this.uY = uY;
        this.name = name;
        App.group.getChildren().remove(App.vbox);
        App.group.getChildren().addAll(this, App.vbox);
    }
}