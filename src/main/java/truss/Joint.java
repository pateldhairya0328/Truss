package truss;

import java.util.ArrayList;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class Joint extends Circle{
    private double uX = 0, uY = 0;
    private double angle = 90;
    private ArrayList<Beam> attachedBeams = new ArrayList<Beam>(0);
    private int type = 4;
    private final String types[] = {"Roller Support", "Pin Support", "Fixed Support", "Joint", "Error"};
    private final Color colors[] = {Color.DEEPSKYBLUE, Color.CRIMSON, Color.FORESTGREEN, Color.DIMGRAY};
    private ArrayList<Double> forcevals = new ArrayList<>();
    private ArrayList<Double> forcedirs = new ArrayList<>();
    private ArrayList<Arrow> arrows = new ArrayList<>();
    private String name = "";
    private Text displayName = new Text();
    
    public Joint(){
        this.displayName.setText(name);
        this.displayName.setStroke(Color.BLACK);
        this.displayName.setStrokeWidth(0.1);
        this.displayName.setFill(Color.WHITE);
        this.displayName.setFont(new Font("DejaVu Sans Mono", 2*this.getRadius()));
        App.group.getChildren().remove(App.vbox);
        App.group.getChildren().addAll(this, displayName, App.vbox);
    }

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
        this.displayName.setText(name);
        this.displayName.setStroke(Color.BLACK);
        this.displayName.setStrokeWidth(0.1);;
        this.displayName.setFill(Color.WHITE);
        this.displayName.setFont(new Font("DejaVu Sans Mono", 2*this.getRadius()));
        this.displayName.setX(this.getCenterX()-this.displayName.getLayoutBounds().getWidth()/2);
        this.displayName.setY(this.getCenterY()+0.625*this.getRadius());
        App.group.getChildren().remove(App.vbox);
        App.group.getChildren().addAll(this, displayName, App.vbox);
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
        displayName.setText(name);
    }

    public Text getDisplayName(){
        return displayName;
    }

    public double getUY(){
        return uY;
    }

    public void setUY(double uY){
        this.uY = uY;
    }

    public double getUX(){
        return uX;
    }

    public void setUX(double uX){
        this.uX = uX;
    }

    public int getType(){
        return type;
    }
    
    public void setType(int type){
        this.type = type;
    }

    public String[] getTypes(){
        return types;
    }

    public ArrayList<Beam> getBeams(){
        return attachedBeams;
    }

    public double getAngle(){
        return angle;
    }

    public void setAngle(double angle){
        this.angle = angle;
    }

    public ArrayList<Double> getForceVals(){
        return forcevals;
    }

    public ArrayList<Double> getForceDirs(){
        return forcedirs;
    }

    public ArrayList<Arrow> getArrows(){
        return arrows;
    }
}