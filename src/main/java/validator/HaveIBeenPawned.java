package validator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.ini4j.Ini;
import org.ini4j.IniPreferences;
import org.javatuples.Triplet;
import org.openqa.selenium.By;		
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

/**
 * Have I Been Pawned file basically checks about EMail ID's on 
 * https://haveibeenpwned.com/ for Security Breaches
 * 
 * Please Ignore throws Exception instead of handling them via try catch blocks
 * @author Pawankumar Dubey
 */
public class HaveIBeenPawned 
{	
	/*All required class variable are declared below*/
	private WebDriver driver;	

	private Ini ini;

	private java.util.prefs.Preferences prefs;

	private ArrayList<String> emails_lst = new ArrayList<String>();

	private String passed;
	private String failed;

	private String xlsxpath;
	private String title;
	private String sleep;
	private String link;
	private String account; 
	private String submit_btn;
	private String pwned_not_found; 
	private String pwned_not_found_txt; 
	private String pwned_found; 
	private String pwned_found_txt;


	/**
	 * Constructor of the Class HaveIBeenPawned i.e. First default method to be executed. 
	 * Responsible for initialization of all declared variable.
	 * Mostly it gets most of its data from config file that has to be as input from user using the Executable Jar.
	 * @param inifile Configuration File Base where all required initialization parameters are stored.
	 * @throws IOException If Ini File path is not present
	 */
	public HaveIBeenPawned(String inifile) throws IOException 
	{	
		driver = new ChromeDriver();

		/*Configuration File :
		 * Helps to avoid hardcoding and keeping software configurable at runtime.*/
		Path pathini = Paths.get(System.getProperty("user.dir"), "src", "main", "resources", inifile);

		ini = new Ini(new File(pathini.toString()));
		prefs = new IniPreferences(ini);

		passed = prefs.node("commons").get("pass", null);
		failed = prefs.node("commons").get("fail", null); 

		/*Report and Output file are in the same XLSX file*/
		xlsxpath = Paths.get(System.getProperty("user.dir"), "src", "main", "resources", prefs.node("commons").get("xlsxpath", null)).toString();
		title = prefs.node("website").get("title", null);
		sleep = prefs.node("commons").get("sleep", null); 

		/*Below method calls keeps EMail List updated at start*/
		getEMailList_XLSX();

		link = prefs.node("website").get("link", null); 
		account = prefs.node("website").get("input_txt", null); 
		submit_btn = prefs.node("website").get("submit_btn", null); 
		pwned_not_found = prefs.node("website").get("pwned_not_found", null); 
		pwned_not_found_txt = prefs.node("website").get("pwned_not_found_txt", null); 
		pwned_found = prefs.node("website").get("pwned_found", null); 
		pwned_found_txt = prefs.node("website").get("pwned_found_txt", null); 

		/*Launches Website with haveibeenpawned.com*/
		driver.get(link);
	}		 

	/**
	 * Method is useful to update entries against EMail ID for Status Result and Detailed Description from Website
	 * @param email_u EMail ID as Reference for Update
	 * @param status_u Status Update for EMail ID i.e. Passed Or Failed
	 * @param detail_u Detail for EMail ID Status based on output from Website.
	 * @throws IOException If xlsxpath is not present
	 */
	public void updateXlSX(String email_u,String status_u,String detail_u) throws IOException
	{
		FileInputStream file = new FileInputStream(new File(xlsxpath));
		XSSFWorkbook workbook = new XSSFWorkbook(file);
		XSSFSheet sheet = workbook.getSheetAt(0);
		for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) 
		{
			Row row = sheet.getRow(rowIndex);
			if (row != null) 
			{
				Cell cell = row.getCell(0);
				if (cell != null)
				{
					if(cell.getStringCellValue().equals(email_u))
					{
						XSSFRow row1 = sheet.getRow(rowIndex);
						XSSFCell cell1 = row1.getCell(1);
						cell1.setCellValue(status_u);
						
						XSSFRow row2 = sheet.getRow(rowIndex);
						XSSFCell cell2 = row2.getCell(2);
						cell2.setCellValue(detail_u);
					}
				}
			}
		}

		file.close();
		FileOutputStream fos =new FileOutputStream(new File(xlsxpath));
		workbook.write(fos);
		fos.close();
		System.out.println("Done");
	}

	/**
	 * Reads XLSX file and fill EMAils List with 'To Be Validated' ID's
	 */
	public void getEMailList_XLSX()
	{
		try
		{	
			FileInputStream file = new FileInputStream(new File(xlsxpath));
			XSSFWorkbook workbook = new XSSFWorkbook(file);
			XSSFSheet sheet = workbook.getSheetAt(0);
			for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) 
			{
				Row row = sheet.getRow(rowIndex);
				if (row != null) 
				{
					Cell cell = row.getCell(0);
					if (cell != null)
					{
						emails_lst.add(cell.getStringCellValue());
					}
				}
			}
			file.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Loops over EMail list and calls validate_single_pawn with single EMail ID's
	 * Return data is sent to update XlSX for reporting purpose.
	 * @throws InterruptedException If Thread.Sleep is interrupted inside validate_single_pawn Method
	 * @throws IOException If xlsxpath is not present inside updateXlSX Method
	 */
	public void validate_all_pawns() throws InterruptedException, IOException
	{
		for(String email : emails_lst)
		{	
			Triplet<String, String, String> triplet = validate_single_pawn(email);
			if(triplet != null)
			{	
				updateXlSX(triplet.getValue0(),triplet.getValue1(),triplet.getValue2());
			}
		}
	}

	/**
	 * Returns web driver instance 
	 * @return Current Web Driver Instance
	 */
	public WebDriver getDriver()
	{
		return driver;
	}

	/**
	 * Method is independent to run email ids on HaveIbeenPawned.com and return its data back as Triplet Class
	 * @param email Email to be checked for Pawn
	 * @return Triplet's String,String,String or null
	 * @throws NumberFormatException If numeric conversion of String Sleep from ini fails
	 * @throws InterruptedException If Thread.Sleep is interrupted 
	 */
	public Triplet<String, String, String> validate_single_pawn(String email) throws NumberFormatException, InterruptedException
	{	
		if(validate_email(email))
		{	
			System.out.println();
			System.out.println();
			System.out.println("#########################################################################################################################");
			String title_rt = driver.getTitle();		
			System.out.println("Title of the Webpage : "+ title);
			assert title_rt == title: "Webpage isn't the same";

			System.out.println("Email Id is Valid : "+ email);
			driver.findElement(By.id(account)).clear();

			WebElement elem_account = driver.findElement(By.id(account));
			elem_account.sendKeys(email);
			Thread.sleep(Integer.parseInt(sleep));

			WebElement elem_submit = driver.findElement(By.id(submit_btn));
			elem_submit.click();
			Thread.sleep(Integer.parseInt(sleep));           

			System.out.println("driver.findElement(By.xpath(pwned_found)).isDisplayed()  "+ driver.findElement(By.xpath(pwned_found)).isDisplayed());
			System.out.println("driver.findElement(By.xpath(pwned_not_found)).isDisplayed() "+driver.findElement(By.xpath(pwned_not_found)).isDisplayed());

			/*
			 * Here; 
			 * if(driver.findElement(By.xpath(pwned_found)).isDisplayed())
			 * searches for //h2[contains(text(),'pwned!')] --> We get this with chrome chropath
			 * If this is found that means Account has been compromised
			 * Below : driver.findElement(By.xpath(pwned_found_txt)).getText().toString(); uses
			 * //p[@id='pwnCount'] to get text for reference
			 * failed data variable has static data as Oops; Email is pawned
			 */
			if(driver.findElement(By.xpath(pwned_found)).isDisplayed())
			{	
				String data = driver.findElement(By.xpath(pwned_found_txt)).getText().toString();
				System.out.println("failed "+ data);
				return Triplet.with(email, failed, data);
			}
			/*
			 * Here; 
			 * if(driver.findElement(By.xpath(pwned_not_found)).isDisplayed())
			 * searches for //h2[contains(text(),'no pwnage found!')] --> We get this with chrome chropath
			 * If this is found that means Account has not been compromised
			 * Below : driver.findElement(By.xpath(pwned_not_found_txt)).getText().toString(); uses
			 * //p[contains(text(),'No')] to get text for reference
			 * passed data variable has static data as Great; Email is not pawned
			 */
			else if(driver.findElement(By.xpath(pwned_not_found)).isDisplayed())
			{	
				String data = driver.findElement(By.xpath(pwned_not_found_txt)).getText().toString();
				System.out.println("passed "+ data);
				return Triplet.with(email, passed, data);
			}
			return null;
		}
		else
		{
			System.out.println('#'*80);
			System.out.println("EMail Id is not valid "+ email);
			return null;
		}
	}	

	/**
	 * Validate EMail ID if applicable to be sent on  HaveIBeenPawned.com 
	 * @param email EMail ID to be Validated
	 * @return boolean
	 */
	public static boolean validate_email(String email)
	{
		String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+ 
				"[a-zA-Z0-9_+&*-]+)*@" + 
				"(?:[a-zA-Z0-9-]+\\.)+[a-z" + 
				"A-Z]{2,7}$"; 

		Pattern pat = Pattern.compile(emailRegex); 
		if (email == null)
		{
			return false; 
		}
		return pat.matcher(email).matches(); 
	}

	/**
	 * Main Function
	 * @param args Command Line Arguments 1. conf.ini 2. emailid@something.com
	 * @throws IOException
	 * @throws NumberFormatException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws IOException, NumberFormatException, InterruptedException 
	{	 
		/*
		 * If only one Argument i.e. conf.ini then whole list of EMail Id's will be validated
		 */
		if(args.length == 1)
		{
			HaveIBeenPawned  hibp = new HaveIBeenPawned(args[0].toString());
			WebDriver driver = hibp.getDriver();
			hibp.validate_all_pawns();
			driver.quit();
		}
		/*
		 * If two Arguments i.e. conf.ini and EMail ID are provided
		 */
		else if(args.length == 2)
		{
			HaveIBeenPawned  hibp = new HaveIBeenPawned(args[0].toString());
			WebDriver driver = hibp.getDriver();
			hibp.validate_single_pawn(args[1].toString());
			driver.quit();
		}
		else
		{
			System.out.println("Acceptable Arguments are only 1. Configuration(InI file) and/or 2. EMail ID "+ args.length);
		}
	}
}
