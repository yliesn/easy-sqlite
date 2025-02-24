import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;

public class ComplexApp extends JFrame {

    private JTabbedPane tabbedPane;
    private JPanel employeePanel;
    private JPanel departmentPanel;
    private JPanel projectPanel;

    private JTextField nomField;
    private JTextField ageField;
    private JTextField departmentField;
    private JTextArea employeeResultArea;

    private JTextField departmentNameField;
    private JTextArea departmentResultArea;

    private JTextField projectNameField;
    private JTextField projectBudgetField;
    private JTextArea projectResultArea;

    public ComplexApp() {
        setTitle("Gestion d'entreprise");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(600, 400));

        // Onglets
        tabbedPane = new JTabbedPane();
        employeePanel = createEmployeePanel();
        departmentPanel = createDepartmentPanel();
        projectPanel = createProjectPanel();

        tabbedPane.addTab("Employés", employeePanel);
        tabbedPane.addTab("Départements", departmentPanel);
        tabbedPane.addTab("Projets", projectPanel);

        add(tabbedPane);
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel createEmployeePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 5, 5);

        // Champs de saisie
        JLabel nomLabel = new JLabel("Nom:");
        constraints.gridx = 0;
        constraints.gridy = 0;
        panel.add(nomLabel, constraints);

        nomField = new JTextField(20);
        constraints.gridx = 1;
        constraints.gridy = 0;
        panel.add(nomField, constraints);

        JLabel ageLabel = new JLabel("Âge:");
        constraints.gridx = 0;
        constraints.gridy = 1;
        panel.add(ageLabel, constraints);

        ageField = new JTextField(20);
        constraints.gridx = 1;
        constraints.gridy = 1;
        panel.add(ageField, constraints);

        JLabel departmentLabel = new JLabel("Département:");
        constraints.gridx = 0;
        constraints.gridy = 2;
        panel.add(departmentLabel, constraints);

        departmentField = new JTextField(20);
        constraints.gridx = 1;
        constraints.gridy = 2;
        panel.add(departmentField, constraints);

        // Boutons
        JButton ajouterBtn = new JButton("Ajouter");
        ajouterBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ajouterEmploye();
            }
        });
        constraints.gridx = 0;
        constraints.gridy = 3;
        panel.add(ajouterBtn, constraints);

        JButton afficherBtn = new JButton("Afficher");
        afficherBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                afficherEmployes();
            }
        });
        constraints.gridx = 1;
        constraints.gridy = 3;
        panel.add(afficherBtn, constraints);

        // Zone de résultats
        employeeResultArea = new JTextArea(10, 40);
        employeeResultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(employeeResultArea);
        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.gridwidth = 2;
        panel.add(scrollPane, constraints);

        return panel;
    }

    private JPanel createDepartmentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 5, 5);

        // Champs de saisie
        JLabel departmentNameLabel = new JLabel("Nom du département:");
        constraints.gridx = 0;
        constraints.gridy = 0;
        panel.add(departmentNameLabel, constraints);

        departmentNameField = new JTextField(20);
        constraints.gridx = 1;
        constraints.gridy = 0;
        panel.add(departmentNameField, constraints);

        // Boutons
        JButton ajouterDepartmentBtn = new JButton("Ajouter");
        ajouterDepartmentBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ajouterDepartement();
            }
        });
        constraints.gridx = 0;
        constraints.gridy = 1;
        panel.add(ajouterDepartmentBtn, constraints);

        JButton afficherDepartmentsBtn = new JButton("Afficher");
        afficherDepartmentsBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                afficherDepartements();
            }
        });
        constraints.gridx = 1;
        constraints.gridy = 1;
        panel.add(afficherDepartmentsBtn, constraints);

        // Zone de résultats
        departmentResultArea = new JTextArea(10, 40);
        departmentResultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(departmentResultArea);
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 2;
        panel.add(scrollPane, constraints);

        return panel;
    }

    private JPanel createProjectPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 5, 5);

        // Champs de saisie
        JLabel projectNameLabel = new JLabel("Nom du projet:");
        constraints.gridx = 0;
        constraints.gridy = 0;
        panel.add(projectNameLabel, constraints);

        projectNameField = new JTextField(20);
        constraints.gridx = 1;
        constraints.gridy = 0;
        panel.add(projectNameField, constraints);

        JLabel projectBudgetLabel = new JLabel("Budget du projet:");
        constraints.gridx = 0;
        constraints.gridy = 1;
        panel.add(projectBudgetLabel, constraints);

        projectBudgetField = new JTextField(20);
        constraints.gridx = 1;
        constraints.gridy = 1;
        panel.add(projectBudgetField, constraints);

        // Boutons
        JButton ajouterProjectBtn = new JButton("Ajouter");
        ajouterProjectBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ajouterProjet();
            }
        });
        constraints.gridx = 0;
        constraints.gridy = 2;
        panel.add(ajouterProjectBtn, constraints);

        JButton afficherProjectsBtn = new JButton("Afficher");
        afficherProjectsBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                afficherProjets();
            }
        });
        constraints.gridx = 1;
        constraints.gridy = 2;
        panel.add(afficherProjectsBtn, constraints);

        // Zone de résultats
        projectResultArea = new JTextArea(10, 40);
        projectResultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(projectResultArea);
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 2;
        panel.add(scrollPane, constraints);

        return panel;
    }

    private void ajouterEmploye() {
        String nom = nomField.getText();
        int age = Integer.parseInt(ageField.getText());
        String departement = departmentField.getText();

        String insertQuery = "INSERT INTO employees (nom, age, departement) VALUES (?, ?, ?)";
        boolean success = ConnectSqLite.executeInsert(insertQuery, nom, age, departement);

        if (success) {
            employeeResultArea.setText("Employé ajouté avec succès");
        } else {
            employeeResultArea.setText("Erreur lors de l'ajout de l'employé");
        }

        nomField.setText("");
        ageField.setText("");
        departmentField.setText("");
    }

    private void afficherEmployes() {
        String selectQuery = "SELECT * FROM employees";
        List<Map<String, Object>> employees = ConnectSqLite.executeSelectAndGetResults(selectQuery);

        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> employee : employees) {
            sb.append("Nom: ")
              .append(employee.get("nom"))
              .append(", Âge: ")
              .append(employee.get("age"))
              .append(", Département: ")
              .append(employee.get("departement"))
              .append("\n");
        }

        employeeResultArea.setText(sb.toString());
    }

    private void ajouterDepartement() {
        String nomDepartement = departmentNameField.getText();

        String insertQuery = "INSERT INTO departments (nom) VALUES (?)";
        boolean success = ConnectSqLite.executeInsert(insertQuery, nomDepartement);

        if (success) {
            departmentResultArea.setText("Département ajouté avec succès");
        } else {
            departmentResultArea.setText("Erreur lors de l'ajout du département");
        }

        departmentNameField.setText("");
    }

    private void afficherDepartements() {
        String selectQuery = "SELECT * FROM departments";
        List<Map<String, Object>> departments = ConnectSqLite.executeSelectAndGetResults(selectQuery);

        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> department : departments) {
            sb.append("Nom: ")
              .append(department.get("nom"))
              .append("\n");
        }

        departmentResultArea.setText(sb.toString());
    }

    private void ajouterProjet() {
        String nomProjet = projectNameField.getText();
        double budget = Double.parseDouble(projectBudgetField.getText());

        String insertQuery = "INSERT INTO projects (nom, budget) VALUES (?, ?)";
        boolean success = ConnectSqLite.executeInsert(insertQuery, nomProjet, budget);

        if (success) {
            projectResultArea.setText("Projet ajouté avec succès");
        } else {
            projectResultArea.setText("Erreur lors de l'ajout du projet");
        }

        projectNameField.setText("");
        projectBudgetField.setText("");
    }

    private void afficherProjets() {
        String selectQuery = "SELECT * FROM projects";
        List<Map<String, Object>> projects = ConnectSqLite.executeSelectAndGetResults(selectQuery);

        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> project : projects) {
            sb.append("Nom: ")
              .append(project.get("nom"))
              .append(", Budget: ")
              .append(project.get("budget"))
              .append("\n");
        }

        projectResultArea.setText(sb.toString());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ComplexApp().setVisible(true);
            }
        });
    }

}