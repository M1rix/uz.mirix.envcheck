package uz.mirix.envcheck;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

public final class EnvRule {
    private final String name; private final String propertyKey; private final boolean required; private final boolean allowBlank; private final boolean sensitive; private final EnvType type; private final Integer minLength; private final Integer maxLength; private final Pattern pattern; private final Set<String> allowedValues; private final BigDecimal min; private final BigDecimal max; private final List<CustomValidator> customValidators;
    private EnvRule(Builder b) {
        this.name=requireText(b.name,"name"); this.propertyKey=b.propertyKey==null?name:requireText(b.propertyKey,"propertyKey"); this.required=b.required; this.allowBlank=b.allowBlank; this.sensitive=b.sensitive; this.type=Objects.requireNonNull(b.type,"type"); this.minLength=nonNegative(b.minLength,"minLength"); this.maxLength=nonNegative(b.maxLength,"maxLength");
        if(minLength!=null&&maxLength!=null&&minLength>maxLength) throw new IllegalArgumentException("minLength cannot be greater than maxLength");
        this.pattern=b.pattern; this.allowedValues=Set.copyOf(b.allowedValues); this.min=b.min; this.max=b.max;
        if(min!=null&&max!=null&&min.compareTo(max)>0) throw new IllegalArgumentException("min cannot be greater than max");
        this.customValidators=List.copyOf(b.customValidators);
    }
    private static String requireText(String v,String f){ if(v==null||v.isBlank()) throw new IllegalArgumentException(f+" must not be blank"); return v; }
    private static Integer nonNegative(Integer v,String f){ if(v!=null&&v<0) throw new IllegalArgumentException(f+" must be >= 0"); return v; }
    public static Builder builder(String name){ return new Builder(name); }
    public String name(){return name;} public String propertyKey(){return propertyKey;} public boolean required(){return required;} public boolean allowBlank(){return allowBlank;} public boolean sensitive(){return sensitive;} public EnvType type(){return type;} public Integer minLength(){return minLength;} public Integer maxLength(){return maxLength;} public Pattern pattern(){return pattern;} public Set<String> allowedValues(){return allowedValues;} public BigDecimal min(){return min;} public BigDecimal max(){return max;} public List<CustomValidator> customValidators(){return customValidators;}
    public static final class Builder {
        private final String name; private String propertyKey; private boolean required=true; private boolean allowBlank; private boolean sensitive; private EnvType type=EnvType.STRING; private Integer minLength; private Integer maxLength; private Pattern pattern; private final Set<String> allowedValues=new LinkedHashSet<>(); private BigDecimal min; private BigDecimal max; private final List<CustomValidator> customValidators=new ArrayList<>();
        private Builder(String name){this.name=name;}
        public Builder property(String v){propertyKey=v;return this;} public Builder required(boolean v){required=v;return this;} public Builder optional(){required=false;return this;} public Builder allowBlank(boolean v){allowBlank=v;return this;} public Builder sensitive(boolean v){sensitive=v;return this;} public Builder type(EnvType v){type=Objects.requireNonNull(v);return this;} public Builder minLength(int v){minLength=v;return this;} public Builder maxLength(int v){maxLength=v;return this;} public Builder pattern(String v){pattern=Pattern.compile(Objects.requireNonNull(v));return this;} public Builder allowedValues(Collection<String> v){allowedValues.addAll(v);return this;} public Builder allowedValues(String... v){allowedValues.addAll(List.of(v));return this;} public Builder min(Number v){min=new BigDecimal(Objects.requireNonNull(v).toString());return this;} public Builder max(Number v){max=new BigDecimal(Objects.requireNonNull(v).toString());return this;} public Builder validateWith(CustomValidator v){customValidators.add(Objects.requireNonNull(v));return this;} public EnvRule build(){return new EnvRule(this);}
    }
}
