/*Created so that the "value" of the object can be changed
currently the value can either be int or string, can be added on to later*/
public class Operand {
	boolean isInt = false;
	boolean isString = false;
	String strValue;
	int intValue;

	Operand(int value) {
		isInt = true;
		intValue = value;
	}

	Operand(String value) {
		isString = true;
		strValue = value;
	}

	public String toString() {
		if (isInt)
			return String.valueOf(intValue);

		return strValue;
	}

	public boolean isInt() {
		return isInt;
	}

	public boolean isString() {
		return isString;
	}
}