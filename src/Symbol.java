public class Symbol {
	SymbolType type;
	private String identifier;
	private String value;
	private int offset;
	public Symbol(SymbolType newType) {
		this.type = newType;
	}
	public void setType(SymbolType type) {
		this.type = type;
	}
	public SymbolType getType() {
		return this.type;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	public String getIdentifier() {
		return this.identifier;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getValue() {
		return this.value;
	}
	public void setOffset(int offset) {
		this.offset = offset;
	}
	public int getOffset() {
		return offset;
	}
}
