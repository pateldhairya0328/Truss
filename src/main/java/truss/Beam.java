package truss;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

public class Beam extends Polygon{
    private Joint A = null;
    private Joint B = null;
    private String name = "";
    private String force = " - ";

    public Beam(){
        this.toBack();
        App.group.getChildren().removeAll(A, B, A.getDisplayName(), B.getDisplayName(), App.vbox);
        App.group.getChildren().addAll(this, A, B, A.getDisplayName(), B.getDisplayName(), App.vbox);
    }

    public Beam(Joint A, Joint B){
        this.name = A.getName()+"-"+B.getName();
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
        App.group.getChildren().removeAll(A, B, A.getDisplayName(), B.getDisplayName(), App.vbox);
        this.A = A;
        this.B = B;
        A.getBeams().add(this);
        B.getBeams().add(this);
        App.beams.add(this);
        this.toBack();
        App.group.getChildren().addAll(this, A, B, A.getDisplayName(), B.getDisplayName(), App.vbox);
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

    public Joint getJointA(){
        return A;
    }

    public Joint getJointB(){
        return B;
    }

    public void setJointA(Joint A){
        this.A = A;
    }

    public void setJointB(Joint B){
        this.B = B;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getForce(){
        return force;
    }

    public void setForce(String force){
        this.force = force;
    }
}