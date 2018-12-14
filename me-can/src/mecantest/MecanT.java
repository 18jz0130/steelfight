package mecantest;

import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorMode;
import lejos.robotics.RegulatedMotor;
import lejos.utility.Delay;

public class MecanT {
	static RegulatedMotor armMotor = Motor.A;
    static RegulatedMotor leftMotor = Motor.B;
    static RegulatedMotor rightMotor = Motor.C;
    static final EV3ColorSensor colorSensor = new EV3ColorSensor(SensorPort.S1);
    static final EV3UltrasonicSensor sonicSensor = new EV3UltrasonicSensor(SensorPort.S2);
    //static final EV3GyroSensor gyroSensor = new EV3GyroSensor(SensorPort.S3);

    static double r_p = 0.114;
    static final int max_speed = 720;
	static boolean is_catch = false;
	static SensorMode color = colorSensor.getMode(2);
	protected enum Story{
		isready,
    	start,
    	line_trace,
    	circle_straight,
    	search,
    	gyro_straight,
    	work_catch
    };

	//static SensorMode gyro = gyroSensor.getMode(1);
	//static float gyrovalue[] = new float[gyro.sampleSize()];
    //Story story = Story.isready;
    static Story story = Story.isready;
    final static float value[][] = new float[6][color.sampleSize()]; //5色 + 現在取る値
	public static void main(String[] args) {

		all_init();
		LCD.clear();
		while ( ! Button.ESCAPE.isDown()){

			//LCD.refresh();
			//touch.fetchSample(value,0);
			story = story(story);
			switch(story) {
			case isready:
				if( Button.RIGHT.isDown() ) {
					armMotor.rotate(20);
				}
				if (Button.LEFT.isDown() ){
					armMotor.rotate(-20);
				}
				LCD.drawString("Ready",0,0);
				break;
			case start:
				//motor_set(30,30);
				break;
			case line_trace:		//ラインを探してトレースする、青が見つかるまで
				//line_trace((float)1.0);
				line_trace_debug(720, 690);
				break;
			case circle_straight:
				if(get_ran_metor() < 20) {
					gyro_trace(max_speed);
				}else {
					gyro_trace(-max_speed);
				}
				break;
			case search:

				break;
			case gyro_straight:

				break;
			case work_catch:
				work_Catch();
				break;
			}
			LCD.drawString("" + story + "          ", 0, 6);
			Delay.msDelay(5);
		}
	}
	public static Story story(Story story) {


		switch (story) {
			case isready:
				if (Button.ENTER.isDown()) {
					story = Story.start;
				}
				break;

			case start://スタート、黄色を外れるまで
				//if ( get_color() == 1) {
					//story = Story.search;
				//}
				break;
			case line_trace://青が見つかるまで
				if(get_color() == 3) {
					story = Story.gyro_straight;
				}
				/*if(get_ss_metor() < 0.05) {
					story = Story.work_catch;
				}*/
				break;
			case circle_straight://サークル内をまっすぐ走る
				/*if() {

				}*/
				break;
			case search://ワークを探す
				gyro_angle();
				/*if (Search()) {
					story = Story.jyro_straight;
				}*/
				break;
			case gyro_straight:

				break;
			case work_catch:
				if (armMotor.isStalled() ) {
					story = Story.line_trace;
				}
				break;
		}
		return story;
	}
	public static float get_ran_metor() {
		int ran_metor = 0;
		final MyBool b = new MyBool();
		if(b.getBool()) {
			b.setBool(false);
			leftMotor.resetTachoCount();
			rightMotor.resetTachoCount();
		}else {
			ran_metor = (leftMotor.getTachoCount() + rightMotor.getTachoCount()) / 2;
		}

		return ran_metor;
	}
	static class MyBool {
        boolean b = true;
        public void setBool(boolean b) {
            this.b = b;
        }
        public boolean getBool() {
            return b;
        }
    }
	public static void work_Catch() {
		motor_set(0,0);
		is_catch = arm_control(is_catch);
	}
	public static boolean Search() {
		float angle_min = 0;
		boolean is_look = false;
		int a = 0;
		int b = 0;
		switch(a) {
		case 0:
			motor_set(0, 0);
			a = 99;
			b = 1;
			break;
		case 1:
			leftMotor.rotate(-135);
			rightMotor.rotate(135);
			a = 99;
			b = 2;
			break;
		case 2:
			leftMotor.rotate(90);
			rightMotor.rotate(-90);
			a = 99;
			b = 3;
			break;
		case 3:
			leftMotor.rotate(-10);
			rightMotor.rotate(10);
			if (angle_min - 3 < gyro_angle() &&  gyro_angle() < angle_min + 3) {
				is_look = true;
			}
			break;
		case 99:
			if( ! leftMotor.isMoving() && ! rightMotor.isMoving()) {
				a = b;
			}
			break;
		}
		if(a != 3 && get_ss_metor() < 20.0) {
			angle_min = gyro_angle();
		}
		return is_look;
	}
	public static float gyro_angle() {
		/*for (int k = 0 ; k < gyro.sampleSize() ; k++) {
			LCD.drawString("val[" + k +"] :" + value[k] +" m", 1, k + 1);
		}*/
		return 1.0f;
	}
	public static void gyro_trace(int speed) {
		float angle = gyro_angle();
		angle= (float)speed / 360.0f;
		motor_set(speed - (int)angle, speed - (int)angle);
	}
	public static float get_ss_metor() {
		SensorMode sonic = sonicSensor.getMode(0);
		float value[] = new float[sonic.sampleSize()];
		sonic.fetchSample(value, 0);
		/*for (int k = 0 ; k < sonic.sampleSize() ; k++) {
			LCD.drawString("val[" + k +"] :" + value[k] +" m", 1, k + 5);
		}*/
		return value[0];
	}
	public static void all_init() {
		motor_init();
		LCD.clear();
	}
	public static boolean arm_control(boolean is_catch) {
		if(is_catch == false) {
			armMotor.rotateTo(0);
			is_catch = true;
		}
		else {
			armMotor.rotateTo(-360);
			is_catch = false;
		}
		Delay.msDelay(5000);
		LCD.drawString("" + armMotor.isStalled() , 0, 5);
		return is_catch;

	}
	private static void motor_init() {
		leftMotor.resetTachoCount();
		leftMotor.rotateTo(0);
		rightMotor.resetTachoCount();
		rightMotor.rotateTo(0);
		armMotor.resetTachoCount();
		armMotor.rotateTo(0);
	}
	public static void line_trace(float value) {
		//float sum = (value[0] + value[1] + value[2]) / 3;

		int p = (int) (10 * (0.114 - value));
		LCD.drawString("p = " + p, 0, 5);

	}
	public static void line_trace_debug(int l_motor_pow, int r_motor_pow) {
		if( get_color() == 2) {
			motor_set(l_motor_pow,r_motor_pow);
		}else{
			motor_set(r_motor_pow,l_motor_pow);
		}
	}
	public static void motor_set (int l_motor_pow, int r_motor_pow) {
		leftMotor.setSpeed(l_motor_pow);
		rightMotor.setSpeed(r_motor_pow);
		leftMotor.forward();
		rightMotor.forward();
	}
	public static int get_color() {
		color.fetchSample(value[6], 0);
		int now_color = 0;
		String colors[] = {
			"white",
			"yellow",
			"green",
			"blue",
			"red",
			"エラー"
		};
		for (int k = 0 ; k < color.sampleSize(); k++) {
			 LCD.drawString("val[" + k +"]" + value[k], 1, k+1);
		}
		/*if(0.41 < value[1] && value[1] < 0.47 ) {
			now_color = 0;
		}else if(0.18 < value[1] && value[1] < 0.24 ) {
			if(0.1 < value[0]) {
				now_color = 1;
			}else {
				now_color = 2;
			}
		}else if(0.06 < value[1] && value[1] < 0.08 ) {
			now_color = 4;
		}else {
			now_color = 5;
		}*/
		if(0.41 < value[6][1] && value[6][1] < 0.47 ) {
			now_color = 0;
		} else if(0.18 < value[6][1] && value[6][1] < 0.24 ) {
			now_color = 2;
		}else {
			now_color = 1;
		}
		LCD.drawString("" +colors[now_color], 0, 7);
		return now_color;
	}
	public static void colors_init() {

		int now_color = 0;
		String colors[] = {
			"white",
			"yellow",
			"green",
			"blue",
			"red",
		};
		if( Button.RIGHT.isDown() ) {
			now_color++;
			if(now_color > 4) {
				now_color = 0;
			}
		}
		if (Button.LEFT.isDown() ){
			now_color--;
			if(now_color < 0) {
				now_color = 4;
			}
		}
		if(Button.ENTER.isDown()) {
			color.fetchSample(value[now_color], 0);
		}

		LCD.drawString("" +colors[now_color], 0, 4);
		for (int k = 0 ; k < color.sampleSize(); k++) {
			 LCD.drawString("val[" + k +"]" + value[now_color][k], 1, k+5);
		}
	}
}
