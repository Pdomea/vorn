package app.beans;

public final class BeanAttributes {
    public static final String FLASH_MESSAGE_BEAN = "flashMessageBean";

    private BeanAttributes() {
    }

    public static String forClass(Class<?> beanClass) {
        if (beanClass == null) {
            throw new IllegalArgumentException("Bean-Klasse ist erforderlich.");
        }
        String simpleName = beanClass.getSimpleName();
        if (simpleName.isEmpty()) {
            throw new IllegalArgumentException("Bean-Klassenname darf nicht leer sein.");
        }
        return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    }
}

