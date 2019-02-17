package vincent;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/*
* FileChooserDemo.java uses these files:
* images/Open16.gif
* images/Save16.gif
*/
public class AESGUI extends JPanel implements ActionListener {
	static private final String newline = "\n";
	JButton chooseFileButton;
	JButton encryptButton;
	JButton decryptButton;
	JLabel passLabel;
	JPasswordField passwordField;
	JTextArea log;
	JFileChooser fc;
	private File fileChosen;
	// private static SecretKeySpec secretKey;
	private byte[] key;
	public static final String CIPHER_INSTANCE_CODE = "AES/ECB/PKCS5Padding";

	public File getFileChosen() {
		return fileChosen;
	}

	public void setFileChosen(File fileChosen) {
		this.fileChosen = fileChosen;
	}

	public SecretKeySpec getSecretKey(String secretStringKey) {
		MessageDigest sha = null;
		try {
			key = secretStringKey.trim().getBytes("UTF-8");
			sha = MessageDigest.getInstance("SHA-1");
			key = sha.digest(key);
			key = Arrays.copyOf(key, 16);
			SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
			return secretKey;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static byte[] readFileToByteArray(File file) throws Exception {
		BufferedInputStream inStream = new BufferedInputStream(new FileInputStream(file));
		int offset = 0;
		int bufferSize = (int) file.length();
		byte[] buffer = new byte[bufferSize];
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		BufferedOutputStream out = new BufferedOutputStream(bs);
		int byteRead = inStream.read(buffer, offset, buffer.length);
		out.write(buffer, offset, byteRead);
		out.flush();
		inStream.close();
		byte[] result = bs.toByteArray();
		out.close();
		return result;
	}

	public static File writeByteArrayToFile(String newFileFullPath, byte[] byteArray) throws Exception {
		File outputFile = new File(newFileFullPath);
		if (outputFile.exists()) {
			outputFile.delete();
		}
		outputFile.createNewFile();
		BufferedOutputStream outStream = new BufferedOutputStream(new FileOutputStream(outputFile));
		outStream.write(byteArray);
		outStream.flush();
		outStream.close();
		return outputFile;
	}

	public static File encryptFile(File fileToEncrypt, SecretKeySpec secretKey) {
		try {
			// setKey(secretStringKey);
			Cipher cipher = Cipher.getInstance(CIPHER_INSTANCE_CODE);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			byte[] encryptedByteArr = cipher.doFinal(readFileToByteArray(fileToEncrypt));
			String filePath = fileToEncrypt.getPath();
			String newFileFullPath = filePath + "_encrypted";
			return writeByteArrayToFile(newFileFullPath, encryptedByteArr);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static File decryptFile(File fileToDecrypt, SecretKeySpec secretKey) {
		try {
			// setKey(secretStringKey);
			Cipher cipher = Cipher.getInstance(CIPHER_INSTANCE_CODE);
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			byte[] encryptedByteArr = cipher.doFinal(readFileToByteArray(fileToDecrypt));
			String filePath = fileToDecrypt.getPath();
			String newFileFullPath = filePath + "_decrypted";
			return writeByteArrayToFile(newFileFullPath, encryptedByteArr);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public AESGUI() {
		super(new BorderLayout());
		this.fileChosen = null;
		// Create the log first, because the action listeners
		// need to refer to it.
		log = new JTextArea(5, 20);
		log.setMargin(new Insets(5, 5, 5, 5));
		log.setEditable(false);
		JScrollPane logScrollPane = new JScrollPane(log);
		// Create a file chooser
		fc = new JFileChooser();
		// Uncomment one of the following lines to try a different
		// file selection mode. The first allows just directories
		// to be selected (and, at least in the Java look and feel,
		// shown). The second allows both files and directories
		// to be selected. If you leave these lines commented out,
		// then the default mode (FILES_ONLY) will be used.
		//
		// fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		// fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		// Create the open button. We use the image from the JLF
		// Graphics Repository (but we extracted it from the jar).
		// openButton = new JButton("Open a File...",
		// createImageIcon("images/Open16.gif"));
		chooseFileButton = new JButton("Choose File");
		chooseFileButton.addActionListener(this);
		encryptButton = new JButton("Encrypt");
		encryptButton.addActionListener(this);
		decryptButton = new JButton("Decrypt");
		decryptButton.addActionListener(this);
		passLabel = new JLabel("PassCode");
		passwordField = new JPasswordField();
		passwordField.setPreferredSize(new Dimension(150, 25));
		// passwordField.setSize(80, passwordField.getHeight());
		// passwordField.setText("");
		// Create the save button. We use the image from the JLF
		// Graphics Repository (but we extracted it from the jar).
		// saveButton = new JButton("Save File");
		// saveButton.addActionListener(this);
		// For layout purposes, put the buttons in a separate panel
		JPanel buttonPanel = new JPanel(); // use FlowLayout
		buttonPanel.add(chooseFileButton);
		buttonPanel.add(encryptButton);
		buttonPanel.add(decryptButton);
		buttonPanel.add(passLabel);
		buttonPanel.add(passwordField);
		// buttonPanel.add(saveButton);
		// Add the buttons and the log to this panel.
		add(buttonPanel, BorderLayout.PAGE_START);
		add(logScrollPane, BorderLayout.CENTER);
	}

	public void actionPerformed(ActionEvent e) {
		// Handle open button action.
		if (e.getSource() == chooseFileButton) {
			int returnVal = fc.showOpenDialog(AESGUI.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				// This is where a real application would open the file.
				log.append("Opening: " + file.getPath() + " ..." + newline);
				if (file != null) {
					setFileChosen(file);
					log.append(file.getPath() + " is chosen" + newline);
				}
			} else {
				log.append("Open command cancelled by user." + newline);
			}
			log.setCaretPosition(log.getDocument().getLength());
			// log.append("Pass = " + new String(passwordField.getPassword())
			// + newline);
			// Handle save button action.
		} else if (e.getSource() == encryptButton) {
			log.append("encryptButton is clicked" + newline);
			if (getFileChosen() == null) {
				log.append("No file is chosen. Please click the choose file button" + newline);
			} else {
				SecretKeySpec secretKey = getSecretKey(new String(passwordField.getPassword()));
				File encryptedFile = encryptFile(getFileChosen(), secretKey);
				log.append("File is Encrypted : " + encryptedFile.getPath() + " Size : " + encryptedFile.length()
						+ newline);
			}
			// log.append("Pass = " + new String(passwordField.getPassword())
			// + newline);
		} else if (e.getSource() == decryptButton) {
			log.append("decryptButton is clicked" + newline);
			// log.append("Pass = " + new String(passwordField.getPassword())
			// + newline);
			SecretKeySpec secretKey = getSecretKey(new String(passwordField.getPassword()));
			File decryptedFile = decryptFile(getFileChosen(), secretKey);
			log.append(
					"File is Decrypted : " + decryptedFile.getPath() + " Size : " + decryptedFile.length() + newline);
		}
		// else if (e.getSource() == saveButton) {
		// int returnVal = fc.showSaveDialog(AESGUI.this);
		// if (returnVal == JFileChooser.APPROVE_OPTION) {
		// File file = fc.getSelectedFile();
		// // This is where a real application would save the file.
		// log.append("Saving: " + file.getName() + "." + newline);
		// } else {
		// log.append("Save command cancelled by user." + newline);
		// }
		// log.setCaretPosition(log.getDocument().getLength());
		// }
	}

	/** Returns an ImageIcon, or null if the path was invalid. */
	protected static ImageIcon createImageIcon(String path) {
		java.net.URL imgURL = AESGUI.class.getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL);
		} else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}

	/**
	 * Create the GUI and show it. For thread safety, this method should be invoked
	 * from the event dispatch thread.
	 */
	private static void createAndShowGUI() {
		// Create and set up the window.
		JFrame frame = new JFrame("AES GUI");
		frame.setLocation(300, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// Add content to the window.
		frame.add(new AESGUI());
		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		// Schedule a job for the event dispatch thread:
		// creating and showing this application's GUI.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// Turn off metal's use of bold fonts
				UIManager.put("swing.boldMetal", Boolean.FALSE);
				createAndShowGUI();
			}
		});
	}
}