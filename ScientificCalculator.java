import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.math.MathContext;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;

/** A small, classic-looking scientific calculator made with Swing. */
public class ScientificCalculator extends JFrame {
    private final JTextField display = new JTextField("0");
    private final JLabel history = new JLabel(" ", SwingConstants.RIGHT);
    private final JButton angleButton = new JButton("DEG");
    private boolean degrees = true;
    private boolean resultShown = false;

    public ScientificCalculator() {
        super("Scientific Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(new Color(236, 233, 216));
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("SCIENTIFIC CALCULATOR", SwingConstants.CENTER);
        title.setFont(new Font("Dialog", Font.BOLD, 15));
        title.setForeground(new Color(55, 65, 77));
        title.setBorder(BorderFactory.createEmptyBorder(12, 8, 0, 8));
        add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(0, 12, 12, 12));

        JPanel screen = new JPanel(new BorderLayout(0, 2));
        screen.setBackground(new Color(255, 253, 242));
        screen.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(145, 145, 135), 2),
                BorderFactory.createEmptyBorder(7, 9, 7, 9)));
        history.setFont(new Font("Dialog", Font.PLAIN, 12));
        history.setForeground(new Color(105, 105, 100));
        screen.add(history, BorderLayout.NORTH);
        display.setEditable(false);
        display.setFocusable(false);
        display.setBorder(null);
        display.setBackground(new Color(255, 253, 242));
        display.setHorizontalAlignment(JTextField.RIGHT);
        display.setFont(new Font("Monospaced", Font.PLAIN, 25));
        screen.add(display, BorderLayout.CENTER);
        screen.setPreferredSize(new Dimension(0, 77));
        center.add(screen, BorderLayout.NORTH);

        String[][] labels = {
            {"DEG", "sin", "cos", "tan", "√"},
            {"(", ")", "log", "ln", "xʸ"},
            {"x²", "1/x", "π", "e", "!"},
            {"7", "8", "9", "÷", "C"},
            {"4", "5", "6", "×", "⌫"},
            {"1", "2", "3", "−", "±"},
            {"0", ".", "%", "+", "="}
        };
        JPanel keys = new JPanel(new GridLayout(7, 5, 5, 5));
        keys.setOpaque(false);
        for (String[] row : labels) {
            for (String label : row) {
                JButton button = label.equals("DEG") ? angleButton : new JButton(label);
                styleButton(button, label);
                button.addActionListener(event -> press(button.getText()));
                keys.add(button);
            }
        }
        center.add(keys, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
        setPreferredSize(new Dimension(390, 515));
        pack();
        setLocationRelativeTo(null);
        installKeyboardShortcuts();
    }

    private void styleButton(JButton button, String label) {
        button.setFocusable(false);
        button.setFont(new Font("Dialog", Font.BOLD, 16));
        button.setForeground(new Color(42, 48, 54));
        button.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        if (label.equals("=")) {
            button.setBackground(new Color(91, 117, 143));
            button.setForeground(Color.WHITE);
        } else if (label.equals("C") || label.equals("⌫")) {
            button.setBackground(new Color(233, 202, 196));
        } else if ("0123456789.".contains(label) && label.length() == 1) {
            button.setBackground(new Color(251, 250, 245));
        } else if (label.equals("+") || label.equals("−") || label.equals("×") || label.equals("÷")) {
            button.setBackground(new Color(213, 224, 233));
        } else {
            button.setBackground(new Color(226, 222, 207));
        }
    }

    private void installKeyboardShortcuts() {
        String typed = "0123456789.+-*/^()%!";
        for (char key : typed.toCharArray()) {
            String value = String.valueOf(key);
            getRootPane().getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW)
                    .put(KeyStroke.getKeyStroke(key), "type " + value);
            getRootPane().getActionMap().put("type " + value, new AbstractAction() {
                public void actionPerformed(ActionEvent event) { press(value); }
            });
        }
        bindKey("ENTER", "=");
        bindKey("BACK_SPACE", "⌫");
        bindKey("ESCAPE", "C");
    }

    private void bindKey(String key, String button) {
        getRootPane().getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(key), "key " + key);
        getRootPane().getActionMap().put("key " + key, new AbstractAction() {
            public void actionPerformed(ActionEvent event) { press(button); }
        });
    }

    private void press(String button) {
        if (button.equals("DEG") || button.equals("RAD")) {
            degrees = !degrees;
            angleButton.setText(degrees ? "DEG" : "RAD");
            return;
        }
        if (button.equals("C")) {
            display.setText("0");
            history.setText(" ");
            resultShown = false;
            return;
        }
        if (button.equals("⌫")) {
            String value = display.getText();
            display.setText(value.length() <= 1 || value.equals("Error")
                    ? "0" : value.substring(0, value.length() - 1));
            resultShown = false;
            return;
        }
        if (button.equals("=")) {
            calculate();
            return;
        }

        String value = display.getText();
        if (value.equals("Error")) value = "0";
        boolean startsNew = button.matches("[0-9]") || button.equals(".")
                || button.equals("π") || button.equals("e") || button.equals("(")
                || button.equals("sin") || button.equals("cos") || button.equals("tan")
                || button.equals("log") || button.equals("ln") || button.equals("√");
        if (resultShown && startsNew) value = "0";
        resultShown = false;

        if (button.equals("±")) {
            value = value.equals("0") ? "-" : value.startsWith("-")
                    ? value.substring(1) : "-(" + value + ")";
        } else if (button.equals("1/x")) {
            value = "1/(" + value + ")";
        } else {
            String addition;
            switch (button) {
                case "x²": addition = "^2"; break;
                case "xʸ": addition = "^"; break;
                case "×": addition = "×"; break;
                case "÷": addition = "÷"; break;
                case "−": addition = "-"; break;
                case "√": addition = "sqrt("; break;
                case "sin": case "cos": case "tan": case "log": case "ln":
                    addition = button + "("; break;
                default: addition = button;
            }
            if (value.equals("0") && !addition.equals(".") && !addition.equals(")")
                    && !addition.equals("%") && !addition.equals("!")) value = "";
            value += addition;
        }
        display.setText(value);
    }

    private void calculate() {
        String expression = display.getText();
        try {
            // Close any brackets left open while typing a function.
            int open = 0;
            for (char c : expression.toCharArray()) {
                if (c == '(') open++;
                if (c == ')') open--;
                if (open < 0) throw new IllegalArgumentException("Check brackets");
            }
            String complete = expression + ")".repeat(open);
            double answer = evaluateExpression(complete, degrees);
            if (!Double.isFinite(answer)) throw new IllegalArgumentException("Result is too large");
            history.setText(complete + " =");
            display.setText(new BigDecimal(answer, new MathContext(12))
                    .stripTrailingZeros().toPlainString());
            resultShown = true;
        } catch (IllegalArgumentException error) {
            history.setText(error.getMessage());
            display.setText("Error");
            resultShown = false;
        }
    }

    /** Evaluates the same expressions accepted by the calculator buttons. */
    public static double evaluateExpression(String expression, boolean degrees) {
        return new Parser(expression, degrees).parse();
    }

    private static class Parser {
        private final String input;
        private final boolean degrees;
        private int position = 0;

        Parser(String input, boolean degrees) {
            this.input = input.replace(" ", "");
            this.degrees = degrees;
        }

        double parse() {
            double answer = expression();
            if (position != input.length()) throw new IllegalArgumentException("Check expression");
            return answer;
        }

        private double expression() {
            double value = term();
            while (true) {
                if (take('+')) value += term();
                else if (take('-') || take('−')) value -= term();
                else return value;
            }
        }

        private double term() {
            double value = unary();
            while (true) {
                if (take('*') || take('×')) value *= unary();
                else if (take('/') || take('÷')) {
                    double divisor = unary();
                    if (divisor == 0) throw new IllegalArgumentException("Cannot divide by zero");
                    value /= divisor;
                } else if (position < input.length() &&
                        (input.charAt(position) == '(' || input.charAt(position) == 'π'
                        || Character.isLetter(input.charAt(position)))) {
                    value *= unary(); // Allows 2π and 2(3+4).
                } else return value;
            }
        }

        private double unary() {
            if (take('+')) return unary();
            if (take('-') || take('−')) return -unary();
            return power();
        }

        private double power() {
            double value = postfix();
            if (take('^')) value = Math.pow(value, unary());
            return value;
        }

        private double postfix() {
            double value = primary();
            while (true) {
                if (take('%')) value /= 100;
                else if (take('!')) {
                    if (value < 0 || value > 170 || value != Math.rint(value))
                        throw new IllegalArgumentException("Factorial needs 0 to 170");
                    double answer = 1;
                    for (int n = 2; n <= (int) value; n++) answer *= n;
                    value = answer;
                } else return value;
            }
        }

        private double primary() {
            if (take('(')) {
                double value = expression();
                if (!take(')')) throw new IllegalArgumentException("Check brackets");
                return value;
            }
            if (take('π')) return Math.PI;
            if (take('e')) return Math.E;
            int start = position;
            while (position < input.length() && Character.isLetter(input.charAt(position))) position++;
            if (position > start) {
                String name = input.substring(start, position);
                if (!take('(')) throw new IllegalArgumentException("Use brackets after " + name);
                double value = expression();
                if (!take(')')) throw new IllegalArgumentException("Check brackets");
                double angle = degrees ? Math.toRadians(value) : value;
                switch (name) {
                    case "sin": return Math.sin(angle);
                    case "cos": return Math.cos(angle);
                    case "tan":
                        if (Math.abs(Math.cos(angle)) < 1e-12)
                            throw new IllegalArgumentException("Tangent is undefined");
                        return Math.tan(angle);
                    case "sqrt":
                        if (value < 0) throw new IllegalArgumentException("Cannot square root a negative");
                        return Math.sqrt(value);
                    case "log":
                        if (value <= 0) throw new IllegalArgumentException("Log needs a positive number");
                        return Math.log10(value);
                    case "ln":
                        if (value <= 0) throw new IllegalArgumentException("Ln needs a positive number");
                        return Math.log(value);
                    default: throw new IllegalArgumentException("Unknown function");
                }
            }
            start = position;
            boolean dot = false;
            while (position < input.length()) {
                char c = input.charAt(position);
                if (c == '.' && !dot) { dot = true; position++; }
                else if (Character.isDigit(c)) position++;
                else break;
            }
            if (position == start) throw new IllegalArgumentException("Check expression");
            try {
                return Double.parseDouble(input.substring(start, position));
            } catch (NumberFormatException error) {
                throw new IllegalArgumentException("Check number");
            }
        }

        private boolean take(char expected) {
            if (position < input.length() && input.charAt(position) == expected) {
                position++;
                return true;
            }
            return false;
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
        catch (Exception ignored) { /* Use the available Swing look and feel. */ }
        SwingUtilities.invokeLater(() -> new ScientificCalculator().setVisible(true));
    }
}
