package prog2.finalgroup;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.text.AbstractDocument;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.stream.Collectors;

public class MyProgram extends JFrame {
    private List<Citizen> citizens;
    private DefaultTableModel tableModel;
    private int posX = 0, posY = 0;

    public MyProgram() {
        setTitle("");
        setSize(800, 600);
        setMinimumSize(new Dimension(800, 600));
        setMaximumSize(new Dimension(800, 600));
        setPreferredSize(new Dimension(800, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setUndecorated(true);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(211, 211, 211));

        setShape(new RoundRectangle2D.Double(1, 1, getWidth(), getHeight(), 5, 5));

        JPanel titleBar = new RoundedPanel();
        titleBar.setBackground(Color.BLACK);
        titleBar.setLayout(new BorderLayout());
        titleBar.setPreferredSize(new Dimension(getWidth(), 30));

        JPanel windowControls = new JPanel();
        windowControls.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        windowControls.setBackground(Color.BLACK);

        JButton minimizeButton = createTitleBarButton("-");
        minimizeButton.addActionListener(e -> setState(JFrame.ICONIFIED));

        JButton closeButton = createTitleBarButton("X");
        closeButton.addActionListener(e -> System.exit(0));

        windowControls.add(minimizeButton);
        windowControls.add(closeButton);

        titleBar.add(windowControls, BorderLayout.EAST);

        titleBar.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                posX = e.getX();
                posY = e.getY();
            }
        });

        titleBar.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent evt) {
                setLocation(evt.getXOnScreen() - posX, evt.getYOnScreen() - posY);
            }
        });

        add(titleBar, BorderLayout.NORTH);

        citizens = MyProgramUtility.readCitizensFromFile("RES/data.csv");

        String[] columnNames = {"Name", "Address", "Age", "District", "Resident Type", "Gender"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // All cells are non-editable
            }
        };
        JTable table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false); // Prevent column reordering
        loadTableData();

        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setPreferredSize(new Dimension(0, 30));
        tableHeader.setFont(new Font("Arial", Font.BOLD, 12));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new EmptyBorder(10, 15, 10, 15));
        add(scrollPane, BorderLayout.CENTER);

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(new Color(224, 224, 224));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JButton addButton = createButton("Add New Entry");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(addButton, gbc);

        JButton viewButton = createButton("View Profile");
        gbc.gridy = 1;
        panel.add(viewButton, gbc);

        JButton deleteButton = createButton("Delete Entry");
        gbc.gridy = 2;
        panel.add(deleteButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridheight = 3;
        gbc.weightx = 1.0;
        panel.add(Box.createHorizontalStrut(50), gbc);

        JButton statsButton = new JButton("<");
        statsButton.setPreferredSize(new Dimension(30, 30));
        statsButton.setBackground(Color.DARK_GRAY);
        statsButton.setForeground(Color.WHITE);
        statsButton.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        statsButton.addActionListener(e -> showGraph());

        JTextField searchBar = new JTextField("search bar", 10);
        searchBar.setPreferredSize(new Dimension(150, 30));
        searchBar.setHorizontalAlignment(JTextField.CENTER); // Center the placeholder text
        searchBar.setForeground(Color.GRAY);
        searchBar.setBackground(Color.WHITE);
        searchBar.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        searchBar.setMargin(new Insets(5, 5, 5, 5));

        searchBar.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchBar.getText().equals("search bar")) {
                    searchBar.setText("");
                    searchBar.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (searchBar.getText().isEmpty()) {
                    searchBar.setText("search bar");
                    searchBar.setForeground(Color.GRAY);
                }
            }
        });

        searchBar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String searchText = searchBar.getText().toLowerCase();
                    if (!searchText.isEmpty() && !searchText.equals("search bar")) {
                        filterTableBySearch(searchText);
                    }
                }
            }
        });

        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.add(statsButton, BorderLayout.WEST);
        searchPanel.add(searchBar, BorderLayout.CENTER);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(searchPanel, gbc);

        JButton filterButton = createButton("Filter (dropdown)");
        filterButton.setPreferredSize(new Dimension(150, 30));
        gbc.gridy = 1;
        panel.add(filterButton, gbc);

        JButton clearFilterButton = createButton("Clear Filter");
        clearFilterButton.setPreferredSize(new Dimension(150, 30));
        gbc.gridy = 2;
        panel.add(clearFilterButton, gbc);

        add(panel, BorderLayout.SOUTH);

        JPopupMenu filterMenu = new JPopupMenu();
        JMenuItem nameAscItem = new JMenuItem("• Name ↑ (Ascending)");
        JMenuItem nameDescItem = new JMenuItem("   Name ↓ (Descending)");
        JMenuItem addressAscItem = new JMenuItem("•  Address ↑ (Ascending)");
        JMenuItem addressDescItem = new JMenuItem("    Address ↓ (Descending)");
        JMenuItem ageAscItem = new JMenuItem("•   Age ↑ (Ascending)");
        JMenuItem ageDescItem = new JMenuItem("     Age ↓ (Descending)");
        JMenuItem genderMItem = new JMenuItem("•    Gender: Male");
        JMenuItem genderFItem = new JMenuItem("      Gender: Female");

        filterMenu.add(nameAscItem);
        filterMenu.add(nameDescItem);
        filterMenu.add(addressAscItem);
        filterMenu.add(addressDescItem);
        filterMenu.add(ageAscItem);
        filterMenu.add(ageDescItem);
        filterMenu.add(genderMItem);
        filterMenu.add(genderFItem);

        filterButton.addActionListener(e -> filterMenu.show(filterButton, filterButton.getWidth(), filterButton.getHeight()));

        nameAscItem.addActionListener(e -> sortByName(true));
        nameDescItem.addActionListener(e -> sortByName(false));
        addressAscItem.addActionListener(e -> sortByAddress(true));
        addressDescItem.addActionListener(e -> sortByAddress(false));
        ageAscItem.addActionListener(e -> sortByAge(true));
        ageDescItem.addActionListener(e -> sortByAge(false));
        genderMItem.addActionListener(e -> filterByGender('M'));
        genderFItem.addActionListener(e -> filterByGender('F'));

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addNewEntry();
            }
        });

        viewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    Citizen citizen = citizens.get(selectedRow);
                    viewProfile(citizen, selectedRow);
                }
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    citizens.remove(selectedRow);
                    tableModel.removeRow(selectedRow);
                    MyProgramUtility.writeCitizensToFile(citizens, "RES/data.csv");
                }
            }
        });

        clearFilterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                citizens = MyProgramUtility.readCitizensFromFile("RES/data.csv");
                loadTableData();
            }
        });
    }

    private JButton createTitleBarButton(String text) {
        JButton button = new JButton(text);
        button.setForeground(Color.WHITE);
        button.setBackground(Color.BLACK);
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        button.setFocusPainted(false);
        return button;
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(150, 30));
        button.setBackground(Color.DARK_GRAY);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        return button;
    }

    private void loadTableData() {
        tableModel.setRowCount(0);
        for (Citizen citizen : citizens) {
            tableModel.addRow(citizen.toRowData());
        }
    }

    private void filterTableBySearch(String searchText) {
        tableModel.setRowCount(0);
        for (Citizen citizen : citizens) {
            if (citizen.getFullName().toLowerCase().contains(searchText) || citizen.getAddress().toLowerCase().contains(searchText)) {
                tableModel.addRow(citizen.toRowData());
            }
        }
    }

    private void addNewEntry() {
        JTextField firstNameField = new JTextField();
        JTextField lastNameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField addressField = new JTextField();
        JTextField ageField = new JTextField();
        JCheckBox residentCheckBox = new JCheckBox();
        JTextField districtField = new JTextField();
        JComboBox<String> genderComboBox = new JComboBox<>(new String[]{"Male", "Female"});

        ((AbstractDocument) firstNameField.getDocument()).setDocumentFilter(new LetterDocumentFilter());
        ((AbstractDocument) lastNameField.getDocument()).setDocumentFilter(new LetterDocumentFilter());
        ((AbstractDocument) ageField.getDocument()).setDocumentFilter(new NumberDocumentFilter());
        ((AbstractDocument) districtField.getDocument()).setDocumentFilter(new NumberDocumentFilter());

        JPanel panel = new JPanel(new GridLayout(8, 2));
        panel.add(new JLabel("First Name:"));
        panel.add(firstNameField);
        panel.add(new JLabel("Last Name:"));
        panel.add(lastNameField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Address:"));
        panel.add(addressField);
        panel.add(new JLabel("Age:"));
        panel.add(ageField);
        panel.add(new JLabel("Resident:"));
        panel.add(residentCheckBox);
        panel.add(new JLabel("District:"));
        panel.add(districtField);
        panel.add(new JLabel("Gender:"));
        panel.add(genderComboBox);

        int result = JOptionPane.showConfirmDialog(null, panel, "Add New Entry", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String fullName = firstName + " " + lastName;
            String email = emailField.getText().trim();
            String address = addressField.getText().trim();
            String ageText = ageField.getText().trim();
            boolean resident = residentCheckBox.isSelected();
            String districtText = districtField.getText().trim();
            String genderText = (String) genderComboBox.getSelectedItem();

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || address.isEmpty() || ageText.isEmpty() || districtText.isEmpty() || genderText == null) {
                JOptionPane.showMessageDialog(null, "Please enter valid data for all fields.");
                return;
            }

            int age;
            int district;
            char gender;

            try {
                age = Integer.parseInt(ageText);
                district = Integer.parseInt(districtText);
                gender = genderText.charAt(0);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Age and District must be numbers.");
                return;
            }

            Citizen newCitizen = new Citizen(fullName, email, address, age, resident, district, gender);
            citizens.add(newCitizen);
            tableModel.addRow(newCitizen.toRowData());
            MyProgramUtility.writeCitizensToFile(citizens, "RES/data.csv");
        }
    }

    private void viewProfile(Citizen citizen, int index) {
        JFrame frame = new JFrame();
        frame.setSize(600, 350);
        frame.setLayout(new BorderLayout());
        frame.setUndecorated(true);

        JPanel titleBar = new RoundedPanel();
        titleBar.setBackground(Color.BLACK);
        titleBar.setLayout(new BorderLayout());
        titleBar.setPreferredSize(new Dimension(frame.getWidth(), 30));

        JPanel windowControls = new JPanel();
        windowControls.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        windowControls.setBackground(Color.BLACK);

        JButton closeButton = createTitleBarButton("X");
        closeButton.addActionListener(e -> frame.dispose());

        windowControls.add(closeButton);
        titleBar.add(windowControls, BorderLayout.EAST);

        titleBar.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                posX = e.getX();
                posY = e.getY();
            }
        });

        titleBar.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent evt) {
                frame.setLocation(evt.getXOnScreen() - posX, evt.getYOnScreen() - posY);
            }
        });

        frame.add(titleBar, BorderLayout.NORTH);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel imageLabel = new JLabel(new ImageIcon(new ImageIcon("RES/user.png").getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH)));
        panel.add(imageLabel, BorderLayout.WEST);

        JPanel infoPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 10); // Add right insets to reduce gap between image and text fields
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;

        JLabel nameLabel = new JLabel("Name: ");
        JLabel emailLabel = new JLabel("Email: ");
        JLabel addressLabel = new JLabel("Address: ");
        JLabel ageLabel = new JLabel("Age: ");
        JLabel genderLabel = new JLabel("Gender: ");
        JLabel residentLabel = new JLabel("Resident type: ");
        JLabel districtLabel = new JLabel("District: ");

        JTextField nameField = new JTextField(citizen.getFullName(), 30);
        JTextField emailField = new JTextField(citizen.getEmail(), 30);
        JTextField addressField = new JTextField(citizen.getAddress(), 30);
        JTextField ageField = new JTextField(String.valueOf(citizen.getAge()), 30);
        JComboBox<String> genderComboBox = new JComboBox<>(new String[]{"Male", "Female"});
        JComboBox<String> residentComboBox = new JComboBox<>(new String[]{"Resident", "Non-Resident"});
        JTextField districtField = new JTextField(String.valueOf(citizen.getDistrict()), 30);

        genderComboBox.setSelectedItem(String.valueOf(citizen.getGender()));
        residentComboBox.setSelectedItem(citizen.isResident() ? "Resident" : "Non-Resident");

        nameField.setEditable(false);
        emailField.setEditable(false);
        addressField.setEditable(false);
        ageField.setEditable(false);
        genderComboBox.setEnabled(false);
        residentComboBox.setEnabled(false);
        districtField.setEditable(false);

        infoPanel.add(nameLabel, gbc);
        gbc.gridx = 1;
        infoPanel.add(nameField, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        infoPanel.add(emailLabel, gbc);
        gbc.gridx = 1;
        infoPanel.add(emailField, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        infoPanel.add(addressLabel, gbc);
        gbc.gridx = 1;
        infoPanel.add(addressField, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        infoPanel.add(ageLabel, gbc);
        gbc.gridx = 1;
        infoPanel.add(ageField, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        infoPanel.add(genderLabel, gbc);
        gbc.gridx = 1;
        infoPanel.add(genderComboBox, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        infoPanel.add(residentLabel, gbc);
        gbc.gridx = 1;
        infoPanel.add(residentComboBox, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        infoPanel.add(districtLabel, gbc);
        gbc.gridx = 1;
        infoPanel.add(districtField, gbc);

        panel.add(infoPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

        JButton editButton = new JButton("Edit Entry");
        JButton okButton = new JButton("Ok");
        JButton applyButton = new JButton("Apply");
        JButton cancelButton = new JButton("Cancel");

        applyButton.setVisible(false);
        cancelButton.setVisible(false);

        editButton.addActionListener(e -> {
            nameField.setEditable(true);
            emailField.setEditable(true);
            addressField.setEditable(true);
            ageField.setEditable(true);
            genderComboBox.setEnabled(true);
            residentComboBox.setEnabled(true);
            districtField.setEditable(true);
            editButton.setVisible(false);
            okButton.setVisible(true);
            applyButton.setVisible(true);
            cancelButton.setVisible(true);
        });

        applyButton.addActionListener(e -> {
            String fullName = nameField.getText().trim();
            String email = emailField.getText().trim();
            String address = addressField.getText().trim();
            int age = Integer.parseInt(ageField.getText().trim());
            char gender = ((String) genderComboBox.getSelectedItem()).charAt(0);
            boolean resident = residentComboBox.getSelectedItem().equals("Resident");
            int district = Integer.parseInt(districtField.getText().trim());

            Citizen updatedCitizen = new Citizen(fullName, email, address, age, resident, district, gender);
            citizens.set(index, updatedCitizen);
            MyProgramUtility.writeCitizensToFile(citizens, "RES/data.csv");
            loadTableData();
        });

        cancelButton.addActionListener(e -> {
            nameField.setEditable(false);
            emailField.setEditable(false);
            addressField.setEditable(false);
            ageField.setEditable(false);
            genderComboBox.setEnabled(false);
            residentComboBox.setEnabled(false);
            districtField.setEditable(false);
            editButton.setVisible(true);
            okButton.setVisible(true);
            applyButton.setVisible(false);
            cancelButton.setVisible(false);
        });

        okButton.addActionListener(e -> {
            if (applyButton.isVisible()) {
                applyButton.doClick();
            }
            frame.dispose();
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(applyButton);
        buttonPanel.add(okButton);
        buttonPanel.add(editButton);

        frame.add(panel, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void showGraph() {
        JFrame frame = new JFrame();
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout());
        frame.setUndecorated(true);

        JPanel titleBar = new RoundedPanel();
        titleBar.setBackground(Color.BLACK);
        titleBar.setLayout(new BorderLayout());
        titleBar.setPreferredSize(new Dimension(frame.getWidth(), 30));

        JPanel windowControls = new JPanel();
        windowControls.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        windowControls.setBackground(Color.BLACK);

        JButton closeButton = createTitleBarButton("X");
        closeButton.addActionListener(e -> frame.dispose());

        windowControls.add(closeButton);
        titleBar.add(windowControls, BorderLayout.EAST);

        titleBar.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                posX = e.getX();
                posY = e.getY();
            }
        });

        titleBar.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent evt) {
                frame.setLocation(evt.getXOnScreen() - posX, evt.getYOnScreen() - posY);
            }
        });

        frame.add(titleBar, BorderLayout.NORTH);

        JPanel panel = new JPanel(new GridLayout(6, 1, 10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel totalPopulationLabel = new JLabel("Total Population: " + citizens.size());
        JLabel totalMaleLabel = new JLabel("Total Male: " + MyProgramUtility.countByGender(citizens, 'M'));
        JLabel totalFemaleLabel = new JLabel("Total Female: " + MyProgramUtility.countByGender(citizens, 'F'));
        JLabel totalResidentsLabel = new JLabel("Total Residents: " + MyProgramUtility.countResidents(citizens));
        JLabel totalNonResidentsLabel = new JLabel("Total Non-Residents: " + MyProgramUtility.countNonResidents(citizens));

        panel.add(totalPopulationLabel);
        panel.add(totalMaleLabel);
        panel.add(totalFemaleLabel);
        panel.add(totalResidentsLabel);
        panel.add(totalNonResidentsLabel);

        frame.add(panel, BorderLayout.CENTER);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void sortByName(boolean ascending) {
        citizens = citizens.stream()
                .sorted((c1, c2) -> ascending ? c1.getFullName().compareTo(c2.getFullName()) : c2.getFullName().compareTo(c1.getFullName()))
                .collect(Collectors.toList());
        loadTableData();
    }

    private void sortByAddress(boolean ascending) {
        citizens = citizens.stream()
                .sorted((c1, c2) -> ascending ? c1.getAddress().compareTo(c2.getAddress()) : c2.getAddress().compareTo(c1.getAddress()))
                .collect(Collectors.toList());
        loadTableData();
    }

    private void sortByAge(boolean ascending) {
        citizens = citizens.stream()
                .sorted((c1, c2) -> ascending ? Integer.compare(c1.getAge(), c2.getAge()) : Integer.compare(c2.getAge(), c1.getAge()))
                .collect(Collectors.toList());
        loadTableData();
    }

    private void filterByGender(char gender) {
        List<Citizen> filtered = citizens.stream()
                .filter(citizen -> citizen.getGender() == gender)
                .collect(Collectors.toList());
        tableModel.setRowCount(0);
        for (Citizen citizen : filtered) {
            tableModel.addRow(citizen.toRowData());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MyProgram().setVisible(true);
            }
        });
    }

    class RoundedPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private Color backgroundColor;
        private int cornerRadius = 4;

        public RoundedPanel() {
            super();
            setOpaque(false);
            this.backgroundColor = Color.BLACK;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Dimension arcs = new Dimension(cornerRadius, cornerRadius);
            int width = getWidth();
            int height = getHeight();
            Graphics2D graphics = (Graphics2D) g;
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (backgroundColor != null) {
                graphics.setColor(backgroundColor);
            } else {
                graphics.setColor(getBackground());
            }
            graphics.fillRoundRect(0, 0, width - 1, height - 1, arcs.width, arcs.height);
            graphics.setColor(getForeground());
            graphics.drawRoundRect(0, 0, width - 1, height - 1, arcs.width, arcs.height);
        }
    }
}

// Custom DocumentFilter to allow only letters
class LetterDocumentFilter extends javax.swing.text.DocumentFilter {
    @Override
    public void insertString(FilterBypass fb, int offset, String string, javax.swing.text.AttributeSet attr) throws javax.swing.text.BadLocationException {
        if (string != null && string.matches("[a-zA-Z ]*")) {
            super.insertString(fb, offset, string, attr);
        }
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, javax.swing.text.AttributeSet attrs) throws javax.swing.text.BadLocationException {
        if (text != null && text.matches("[a-zA-Z ]*")) {
            super.replace(fb, offset, length, text, attrs);
        }
    }
}

// Custom DocumentFilter to allow only numbers
class NumberDocumentFilter extends javax.swing.text.DocumentFilter {
    @Override
    public void insertString(FilterBypass fb, int offset, String string, javax.swing.text.AttributeSet attr) throws javax.swing.text.BadLocationException {
        if (string != null && string.matches("[0-9]*")) {
            super.insertString(fb, offset, string, attr);
        }
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, javax.swing.text.AttributeSet attrs) throws javax.swing.text.BadLocationException {
        if (text != null && text.matches("[0-9]*")) {
            super.replace(fb, offset, length, text, attrs);
        }
    }
}
