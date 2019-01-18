package truss;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class Beam extends Polygon{
    private Joint A = null;
    private Joint B = null;
    private String name = "";
    private Text force = new Text();

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
        this.force.setX(0.5*(A.getCenterX()+B.getCenterX()));
        this.force.setY(0.5*(A.getCenterY()+B.getCenterY()));
        this.force.setStrokeWidth(0.2);
        this.force.setStroke(Color.WHITE);
        this.force.setFill(new Color(60/255, 60/255, 60/255, 1));
        this.force.setFont(new Font("DejaVu Sans Mono", A.getRadius()));
        App.group.getChildren().removeAll(A, B, A.getDisplayName(), B.getDisplayName(), App.vbox);
        this.A = A;
        this.B = B;
        A.getBeams().add(this);
        B.getBeams().add(this);
        App.beams.add(this);
        this.toBack();
        App.group.getChildren().addAll(this, A, B, force, A.getDisplayName(), B.getDisplayName(), App.vbox);
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
        this.force.setX(0.5*(A.getCenterX()+B.getCenterX()));
        this.force.setY(0.5*(A.getCenterY()+B.getCenterY()));
        this.force.setFont(new Font("DejaVu Sans Mono", A.getRadius()));
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

    public String getForceText(){
        return force.getText();
    }

    public Text getForce(){
        return force;
    }

    public void setForce(String force){
        this.force.setText(force);
    }
}