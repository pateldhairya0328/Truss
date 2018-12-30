package truss;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

public class Beam extends Polygon{
    Joint A = null;
    Joint B = null;
    String name = "";
    String force = " - ";

    public Beam(Joint A, Joint B){
        this.name = A.name+"-"+B.name;
        double dy = A.getCenterY()-B.getCenterY();
        double dx = A.getCenterX()-B.getCenterX();
        double theta = Math.PI/2+Math.atan2(dy, dx);
        dy = (0.5*A.getRadius())*Math.sin(theta);
        dx = (0.5*A.getRadius())*Math.cos(theta);
        this.getPoints().addAll(new Double[]{
            A.getCenterX()+dx, A.getCenterY()+dy,
            B.getCenterX()+dx, B.getCenterY()+dy,
            B.getCenterX()-dx, B.getCenterY()-dy,
            A.getCenterX()-dx, A.getCenterY()-dy
        });
        this.setFill(Color.GRAY);
        App.group.getChildren().removeAll(A, B, A.displayName, B.displayName, App.vbox);
        this.A = A;
        this.B = B;
        A.attachedBeams.add(this);
        B.attachedBeams.add(this);
        App.beams.add(this);
        this.toBack();
    }

    void recalc(){
        this.getPoints().clear();
        double dy = A.getCenterY()-B.getCenterY();
        double dx = A.getCenterX()-B.getCenterX();
        double theta = Math.PI/2+Math.atan2(dy, dx);
        dy = (0.5*A.getRadius())*Math.sin(theta);
        dx = (0.5*A.getRadius())*Math.cos(theta);
        this.getPoints().addAll(new Double[]{
            A.getCenterX()+dx, A.getCenterY()+dy,
            B.getCenterX()+dx, B.getCenterY()+dy,
            B.getCenterX()-dx, B.getCenterY()-dy,
            A.getCenterX()-dx, A.getCenterY()-dy
        });
    }
}