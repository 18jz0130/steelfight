package mecantest;

import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3GyroSensor;
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
    static final EV3GyroSensor gyroSensor = new EV3GyroSensor(SensorPort.S3);



    static final SensorMode gyro = gyroSensor.getMode(1);
    static final SensorMode color = colorSensor.getMode(2);
	static float color_value[][] = new float[6][color.sampleSize()];// 追加
	static int now_color = 0;
	static float angle_min;
    static double r_p = 0.114;
    static final int max_speed = 720;
	static boolean is_catch = false;
	protected enum Story{
		isready,
    	start,
    	line_trace,
    	circle_straight,
    	search,
    	gyro_straight,
    	work_catch,
    	gyro_back_straight
    };

	static float angle_min1 = 9999;
	static boolean is_look = false;
	static int a = 0;	//	サーチ用変数　ここじゃないほうがいい
	static int b = 0;
	static int ran_metor = 0;
	//static SensorMode gyro = gyroSensor.getMode(1);
	//static float gyrovalue[] = new float[gyro.sampleSize()];
    //Story story = Story.isready;
    static Story story = Story.isready;

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
				motor_set(30,30);
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
				Search();
				break;
			case gyro_straight:
				if(get_ran_metor() < 20) {
					motor_set(20,20);
				}
				break;
			case gyro_back_straight:

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
					LCD.clear();
					story = Story.start;
				}
				break;
			case start://スタート、黄色を外れるまで
				if ( get_color() == 1) {
					LCD.clear();
					story = Story.line_trace;
				}
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
				//gyro_angle();
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
	public static void gyro_straight(int metor) {
		motor_set(100,100);
		if (get_ran_metor() > metor){

		}
	}
	public static float get_ran_metor() {
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
		switch(a) {
		case 0:
			motor_set(0, 0);
			leftMotor.resetTachoCount();
			rightMotor.resetTachoCount();
			a = 1;
			b = 1;
			break;
		case 1:
			leftMotor.rotate(100, true);
			rightMotor.rotate(-100, true);
			if(leftMotor.getTachoCount() > 90) {
				leftMotor.resetTachoCount();
				rightMotor.resetTachoCount();
				a = 2;
			}
			break;
		case 2:
			leftMotor.rotate(55, true);
			rightMotor.rotate(-55, true);
			if(leftMotor.getTachoCount() > 45) {
				leftMotor.resetTachoCount();
				rightMotor.resetTachoCount();
				a = 3;
			}
			break;
		case 3:
			leftMotor.rotate(-55, true);
			rightMotor.rotate(55, true);
			if(leftMotor.getTachoCount() < -55) {
				leftMotor.resetTachoCount();
				rightMotor.resetTachoCount();
				a = 3;
			}
			a = 99;
			b = 4;
			break;
		case 4:
			motor_set(20,-20);
			if (leftMotor.getTachoCount() > metor) {
				is_look = true;
				motor_set(0, 0);
			}
			break;
		case 99:

			break;
		}
		LCD.drawString("" + a, 0, 0);
		LCD.drawString("" + is_look, 0, 1);
		LCD.drawString("" + get_ss_metor(), 0, 2);
		LCD.drawString("a" + leftMotor.getLimitAngle(), 0, 3);
		LCD.drawString("right" + rightMotor.isMoving(), 0, 4);
		if((a == 2 || a == 3) && get_ss_metor() < 20.0) {
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
		if (l_motor_pow < 0) {
			leftMotor.backward();
		}else if(l_motor_pow > 0){
			leftMotor.forward();
		}else {
			leftMotor.stop();
		}
		if(r_motor_pow < 0) {
			rightMotor.backward();
		}else if(r_motor_pow > 0){
			rightMotor.forward();
		}else {
			rightMotor.stop();
		}
	}
	public static int get_color() {
		color.fetchSample(color_value[5], 0);
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
			 LCD.drawString("val[" + k +"]" + color_value[k], 1, k+1);
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
		if(0.41 < color_value[5][1] && color_value[5][1] < 0.47 ) {
			now_color = 0;
		} else if(0.18 < color_value[5][1] && color_value[5][1] < 0.24 ) {
			now_color = 2;
		}else {
			now_color = 1;
		}
		LCD.drawString("" +colors[now_color], 0, 7);
		return now_color;
	}
	public static void colors_init() {

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
			color.fetchSample(color_value[now_color], 0);
			color_value[now_color][0] = (int)(color_value[now_color][0] * 100);
			color_value[now_color][1] = (int)(color_value[now_color][1] * 100);
			color_value[now_color][2] = (int)(color_value[now_color][2] * 100);
		}

		LCD.clear();
		LCD.drawString("" +colors[now_color], 0, 0);
		for (int k = 0 ; k < color.sampleSize(); k++) {
			LCD.drawString("val[" + k +"]" + color_value[now_color][k], 3,  1 + k);
		}
		Delay.msDelay(100);
	}
}
