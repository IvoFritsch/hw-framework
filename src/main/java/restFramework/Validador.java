/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Pedrivo
 */
public class Validador {

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX
            = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    public static boolean validaEmail(String emailStr) {
        if (emailStr == null || emailStr.isEmpty()) {
            return true;
        }
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.find();
    }

    public static boolean validaTelefone(String numeroTelefone) {
        if (numeroTelefone == null || numeroTelefone.isEmpty()) {
            return true;
        }
        return numeroTelefone.matches(".((10)|([1-9][1-9]).)\\s9?[6-9][0-9]{3}-[0-9]{4}") ||
                numeroTelefone.matches(".((10)|([1-9][1-9]).)\\s[2-5][0-9]{3}-[0-9]{4}");
    }

    public static boolean validaCelular(String numeroTelefone) {
        if (numeroTelefone == null || numeroTelefone.isEmpty()) {
            return true;
        }
        return numeroTelefone.matches(".((10)|([1-9][1-9]).)\\s9[6-9][0-9]{3}-[0-9]{4}");
    }
    
    public static boolean validaObrigatorio(Object obj, boolean permiteNegativo) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() == String.class) {
            return !((String) obj).trim().isEmpty();
        }
        if (obj.getClass() == Integer.class) {
            if(permiteNegativo)
                return ((Integer) obj) != 0;
            else
                return ((Integer) obj) > 0;
        }
        if (obj.getClass() == Double.class) {
            if(permiteNegativo)
                return ((Double) obj) != 0.0;
            else
                return ((Double) obj) > 0.0;
        }
        return true;
    }
    
    public static boolean validaObrigatorio(Object obj) {
        return validaObrigatorio(obj, false);
    }

    public static boolean validaMinSize(String str, int i) {
        if (str == null) {
            return true;
        }
        return str.length() >= i;
    }

    public static boolean validaMaxSize(String str, int i) {
        if (str == null) {
            return true;
        }
        return str.length() <= i;
    }

    public static boolean validaValoresPossiveis(Object obj, Object... vals) {
        if (obj == null) {
            return false;
        }
        int i = 0, size = vals.length;
        boolean saida = false;
        while (i < size) {
            if (vals[i].getClass() != obj.getClass()) {
                continue;
            }
            if (vals[i].equals(obj)) {
                saida = true;
                break;
            }
            i++;
        }
        return saida;
    }

    public static boolean validaCPF(String CPF) {
        if(CPF == null || CPF.isEmpty()) return true;
        if(CPF.length() != 14) return false;
        if(CPF.charAt(3) != '.' || CPF.charAt(7) != '.' || CPF.charAt(11) != '-') return false;
        CPF = CPF.replaceAll("[^0-9]", "");

        // considera-se erro CPF's formados por uma sequencia de numeros iguais
        if (CPF.equals("00000000000") || CPF.equals("11111111111")
                || CPF.equals("22222222222") || CPF.equals("33333333333")
                || CPF.equals("44444444444") || CPF.equals("55555555555")
                || CPF.equals("66666666666") || CPF.equals("77777777777")
                || CPF.equals("88888888888") || CPF.equals("99999999999")
                || (CPF.length() != 11)) {
            return (false);
        }

        char dig10, dig11;
        int sm, i, r, num, peso;

        // "try" - protege o codigo para eventuais erros de conversao de tipo (int)
        try {
            // Calculo do 1o. Digito Verificador
            sm = 0;
            peso = 10;
            for (i = 0; i < 9; i++) {
                // converte o i-esimo caractere do CPF em um numero:
                // por exemplo, transforma o caractere '0' no inteiro 0         
                // (48 eh a posicao de '0' na tabela ASCII)         
                num = (int) (CPF.charAt(i) - 48);
                sm = sm + (num * peso);
                peso = peso - 1;
            }

            r = 11 - (sm % 11);
            if ((r == 10) || (r == 11)) {
                dig10 = '0';
            } else {
                dig10 = (char) (r + 48); // converte no respectivo caractere numerico
            }
            // Calculo do 2o. Digito Verificador
            sm = 0;
            peso = 11;
            for (i = 0; i < 10; i++) {
                num = (int) (CPF.charAt(i) - 48);
                sm = sm + (num * peso);
                peso = peso - 1;
            }

            r = 11 - (sm % 11);
            if ((r == 10) || (r == 11)) {
                dig11 = '0';
            } else {
                dig11 = (char) (r + 48);
            }

            // Verifica se os digitos calculados conferem com os digitos informados.
            if ((dig10 == CPF.charAt(9)) && (dig11 == CPF.charAt(10))) {
                return (true);
            } else {
                return (false);
            }
        } catch (Exception erro) {
            return (false);
        }
    }

    public static boolean validaCNPJ(String CNPJ) {
        if(CNPJ == null || CNPJ.isEmpty()) return true;
        if(CNPJ.length() != 18) return false;
        CNPJ = CNPJ.replaceAll("[^0-9]", "");
        // considera-se erro CNPJ's formados por uma sequencia de numeros iguais
        if (CNPJ.equals("00000000000000") || CNPJ.equals("11111111111111")
                || CNPJ.equals("22222222222222") || CNPJ.equals("33333333333333")
                || CNPJ.equals("44444444444444") || CNPJ.equals("55555555555555")
                || CNPJ.equals("66666666666666") || CNPJ.equals("77777777777777")
                || CNPJ.equals("88888888888888") || CNPJ.equals("99999999999999")
                || (CNPJ.length() != 14)) {
            return (false);
        }

        char dig13, dig14;
        int sm, i, r, num, peso;

        // "try" - protege o código para eventuais erros de conversao de tipo (int)
        try {
            // Calculo do 1o. Digito Verificador
            sm = 0;
            peso = 2;
            for (i = 11; i >= 0; i--) {
                // converte o i-ésimo caractere do CNPJ em um número:
                // por exemplo, transforma o caractere '0' no inteiro 0
                // (48 eh a posição de '0' na tabela ASCII)
                num = (int) (CNPJ.charAt(i) - 48);
                sm = sm + (num * peso);
                peso = peso + 1;
                if (peso == 10) {
                    peso = 2;
                }
            }

            r = sm % 11;
            if ((r == 0) || (r == 1)) {
                dig13 = '0';
            } else {
                dig13 = (char) ((11 - r) + 48);
            }

            // Calculo do 2o. Digito Verificador
            sm = 0;
            peso = 2;
            for (i = 12; i >= 0; i--) {
                num = (int) (CNPJ.charAt(i) - 48);
                sm = sm + (num * peso);
                peso = peso + 1;
                if (peso == 10) {
                    peso = 2;
                }
            }

            r = sm % 11;
            if ((r == 0) || (r == 1)) {
                dig14 = '0';
            } else {
                dig14 = (char) ((11 - r) + 48);
            }

            // Verifica se os dígitos calculados conferem com os dígitos informados.
            if ((dig13 == CNPJ.charAt(12)) && (dig14 == CNPJ.charAt(13))) {
                return (true);
            } else {
                return (false);
            }
        } catch (Exception erro) {
            return (false);
        }
    }

}
