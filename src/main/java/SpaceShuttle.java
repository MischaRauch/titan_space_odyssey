import src.main.java.titan.Vector3dInterface;
import src.main.java.Vector3d;

public class SpaceShuttle{
    private double fuelMass;
    private double burnRate = 0.0000005;
    private double probeMass;
    private Vector3dInterface ve = new Vector3d(2e4,2e4,2e4); // effective exhaust velocity
    private int stepsize;
    private int secondsInYear = 31536000;
    private Vector3dInterface[] acceleration;
    private double[] mass_flow; // mass flow rate
    private Vector3dInterface[] thrust;
    private Vector3dInterface maxThrust;
    private double totalM;
    private boolean noMass;

    public SpaceShuttle(double massOfProbe, double initialFuelMass, int chosenStepsize) {
        probeMass = massOfProbe;
        fuelMass = initialFuelMass;
        stepsize = chosenStepsize;
        mass_flow = new double[stepsize];
        totalM = totalMass(probeMass, fuelMass);
        noMass = false;
    }
    /**
     * Computes the acceleration for one step.
     */
    public Vector3dInterface calculateThrust(Vector3dInterface direction, double percentage, Vector3dInterface previousVelocity){
        if(noMass) {
            System.out.println("The fuel has run out");
            return null;
        }
        Vector3dInterface thrust = maxThrust.mul(percentage);
        Vector3dInterface acceleration = new Vector3d(thrust.getX() / totalM, thrust.getY() / totalM, thrust.getZ() / totalM);
        Vector3dInterface newVelocity = previousVelocity.add(acceleration);
        reduceMass(thrust,newVelocity);
        return acceleration;
    }
    /**
     * Computes the used mass of the fuel by using the formula: m = F/V
     */
    public void reduceMass(Vector3dInterface force,Vector3dInterface velocity){
        double usedMass = force.norm()/velocity.norm();
        fuelMass = fuelMass - usedMass;
        if(fuelMass==0 || fuelMass<0){
            noMass = true;
        }
        totalM = totalM + fuelMass;

        /**
        fuelMass = fuelMass*Math.pow((1 - burnRate), 1);
        if(fuelMass==0 || fuelMass<0){
            noMass = true;
        }
        totalM = probeMass + fuelMass;*/
    }

    public double getTotalM(){
        return totalM;
    }
    /**
     * Computes the total mass of the shuttle (the mass of the probe + the mass of the fuel).
     */
    public double totalMass(double massOfProbe, double massOfFuel) {
        return massOfProbe + massOfFuel;
    }

    /** Computes the mass flow rate (ṁ) using the formulas:
        m(t) = M + m * (1 - burnRate)^t; ṁ = dm/dt = m * (-1) * (1 - burnRate)^t * ln((1 - burnRate))
     *
     */
    public double[] massFlowRate(double fuelMass, double burnRate, int secondsInYear) {

        double[] ṁ = new double[stepsize];

        for(int i = 0; i<stepsize; i++) {
            ṁ[i] = fuelMass * (-1) * Math.pow((1 - burnRate), secondsInYear) * Math.log((1 - burnRate));
            //System.out.println(ṁ[i]);
            fuelMass = fuelMass * (1 - burnRate);
        }

        return ṁ;
    }

    /**
     * Computes the thrust of the probe by using the formula t = ve * ṁ.
     */
    public Vector3dInterface[] calculateF() {

        double[] ṁ = massFlowRate(fuelMass, burnRate, secondsInYear);
        thrust = new Vector3dInterface[stepsize];

        for(int i = 0; i<ṁ.length; i++) {
        	thrust[i] = ve.mul(ṁ[i]);
        }

        return thrust;

    }

    /**
     * Computes the accelerations.
     */
    public Vector3dInterface[] calculateAccelerations() {

       	calculateF();
        Vector3dInterface[] a  = new Vector3dInterface[thrust.length];
        double totalMass = totalMass(probeMass, fuelMass);

        for(int i = 0; i<a.length; i++) {
            a[i] = new Vector3d(thrust[i].getX()/totalMass, thrust[i].getY()/totalMass, thrust[i].getZ()/totalMass);
 			totalMass = probeMass + fuelMass*Math.pow((1 - burnRate), i+1);
 			//System.out.println(a[i]);
        }

        return a;
    }

    /**
     * Computes the thrust by using the formula Ma = F.
     */
    /*public Vector3dInterface[] computeThrust() {

        fuelMass = fuelMass * (1 - burnRate); //notice that fuelMass will change at each step.
        double allMass = probeMass + fuelMass;
        thrust = new Vector3dInterface[acceleration.length];

        for(int i = 0; i<acceleration.length; i++) {
        thrust[i] = acceleration[i].mul(allMass);
        //System.out.println(thrust[i].toString());
        }

        return thrust;
    }*/

    /**
     * Gets the acceleration from each step's calculations.
     */
    public Vector3dInterface[] setA(Rate a) {
        acceleration = a.getRates();
        return acceleration;
    }

    public Vector3dInterface[] getThrust() {
        return thrust;
    }
     
}