package uz.mirix.envcheck;

import java.util.Locale;
import java.util.Set;

public final class SecretDetector {
    private static final Set<String> TOKENS=Set.of("PASSWORD","PASSWD","SECRET","TOKEN","API_KEY","APIKEY","PRIVATE_KEY","PRIVATEKEY","CREDENTIAL","AUTH","COOKIE");
    private SecretDetector(){}
    public static boolean isSensitive(String name){String n=name.toUpperCase(Locale.ROOT).replace('-','_').replace('.','_');return TOKENS.stream().anyMatch(n::contains);}
}
