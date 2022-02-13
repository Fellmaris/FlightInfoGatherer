import com.opencsv.CSVWriter;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FlightInfoGatherer {

    private String sourceURL = "https://www.fly540.com/flights/startingDestination-to-targetDestination?isoneway=0&currency=KES&depairportcode=departureAirportCode&arrvairportcode=arrivalAirportCode&date_from=departureDate&date_to=returnDate&adult_no=1&children_no=0&infant_no=0&searchFlight=&change_flight=";

    private final String startingDestination = "nairobi";
    private final String targetDestination = "mombasa";
    private final String departureAirportCode = "NBO";
    private final String arrivalAirportCode = "MBA";
    private final long add10Day = 10;
    private final long add20Day = 20;
    private final long tripLengh = 7;

    public void RunInfoGatherer() {
        writeToFile(infoScraper(setUrl(add10Day, departureAirportCode, arrivalAirportCode)));
        writeToFile(infoScraper(setUrl(add20Day, departureAirportCode, arrivalAirportCode)));

    }

    private String setUrl(long whenWillTheTripHappen, String departureAirportCode, String arrivalAirportCode) {
        LocalDate currentDate = java.time.LocalDate.now().plusDays(whenWillTheTripHappen);
        LocalDate returnDate = currentDate.plusDays(tripLengh);
        String departureDate = currentDate.getDayOfWeek().toString().substring(0, 3) + "%2C+" + currentDate.getDayOfMonth() + "+" + currentDate.getMonth().toString().substring(0, 3) + "+" + currentDate.getYear();
        String returnDateString = returnDate.getDayOfWeek().toString().substring(0, 3) + "%2C+" + returnDate.getDayOfMonth() + "+" + returnDate.getMonth().toString().substring(0, 3) + "+" + returnDate.getYear();
        String targetUrl = sourceURL;
        targetUrl = targetUrl.replace("departureAirportCode", departureAirportCode);
        targetUrl = targetUrl.replace("arrivalAirportCode", arrivalAirportCode);
        targetUrl = targetUrl.replace("departureDate", departureDate);
        targetUrl = targetUrl.replace("returnDate", returnDateString);
        targetUrl = targetUrl.replace("startingDestination", startingDestination);
        targetUrl = targetUrl.replace("targetDestination", targetDestination);
        return targetUrl;
    }

    public boolean findExistingPrice(int i, int cardIndexDepart, WebDriver driver, String direction) {
        try {
            driver.findElement(By.xpath("//div[@class='fly5-flights fly5-" + direction + " th']/div[2]/div[" + i + "]/div[1]/div[1]/div[" + cardIndexDepart + "]/div[1]/div[2]/button")).isDisplayed();
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    private List<String[]> infoScraper(String baseUrl) {
        double totalTaxes = 0.0;
        List<String[]> listOfFlights = new ArrayList<>();
        double departureCheapestPrice = 0;
        double returnCheapestPrice = 0;

        System.setProperty("webdriver.gecko.driver", "C:\\Users\\cerni\\IdeaProjects\\ArturoBaigiamasisDarbas\\FlightInfoGatherer\\src\\main\\resources\\geckodriver.exe");
        WebDriver driver = new FirefoxDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().window().maximize();
        driver.get(baseUrl);

        List<WebElement> listOfDepartureFlights = driver.findElements(By.xpath("//div[@class='fly5-flights fly5-depart th']/div[2]/div"));
        List<WebElement> listOfReturnFlights = driver.findElements(By.xpath("//div[@class='fly5-flights fly5-return th']/div[2]/div"));

        String departureDepAirport = driver.findElement(By.xpath("//div[@class='fly5-flights fly5-depart th']/div[@class='fly5-flight']/div[@class='fly5-flfrom']/span")).getText().substring(1, 4);
        String departureArrAirport = driver.findElement(By.xpath("//div[@class='fly5-flights fly5-depart th']/div[@class='fly5-flight']/div[@class='fly5-flto']/span")).getText().substring(1, 4);
        String returnDepAirport = driver.findElement(By.xpath("//div[@class='fly5-flights fly5-return th']/div[@class='fly5-flight']/div[@class='fly5-flfrom']/span")).getText().substring(1, 4);
        String returnArrAirport = driver.findElement(By.xpath("//div[@class='fly5-flights fly5-return th']/div[@class='fly5-flight']/div[@class='fly5-flto']/span")).getText().substring(1, 4);


        for (int i = 1; i <= listOfDepartureFlights.size(); i++) {
            driver.findElement(By.xpath("//div[@class='fly5-flights fly5-depart th']/div[2]/div[" + i + "]")).click();
            boolean departHasBeenClicked = true;
            if (!driver.findElement(By.xpath("//div[@class='fly5-flights fly5-depart th']/div[2]/div[" + i + "]/table/tbody/tr[@class='tr-link']/td[@class='fdetails fpackage fp1']/span[contains(@class,'flprice')]")).getText().equals("SOLD OUT")) {
                departureCheapestPrice = Double.parseDouble(driver.findElement(By.xpath("//div[@class='fly5-flights fly5-depart th']/div[2]/div[" + i + "]/table/tbody/tr[@class='tr-link']/td[@class='fdetails fpackage fp1']/span[@class='flprice']")).getText().replace(",", ""));
            } else {
                continue;
            }
            int cardIndexDepart = 1;
            if (!findExistingPrice(i, cardIndexDepart, driver, "depart")) {
                cardIndexDepart++;
                if (!findExistingPrice(i, cardIndexDepart, driver, "depart")) {
                    cardIndexDepart++;
                    if (!findExistingPrice(i, cardIndexDepart, driver, "depart")) {
                        System.out.println("Depart flight sold out");
                        continue;
                    }
                }
            }
            String departureDepTime = driver.findElement(By.xpath("//div[@class='fly5-flights fly5-depart th']/div[2]/div[" + i + "]/table/tbody/tr[@class='tr-link']/td[@data-title='Departs']/span[@class='fldate']")).getText().replace(",", "") + " " + driver.findElement(By.xpath("//div[@class='fly5-flights fly5-depart th']/div[2]/div[" + i + "]/table/tbody/tr[@class='tr-link']/td[@data-title='Departs']/span[@class='fltime ftop']")).getText();
            String departureArrTime = driver.findElement(By.xpath("//div[@class='fly5-flights fly5-depart th']/div[2]/div[" + i + "]/table/tbody/tr[@class='tr-link']/td[@data-title='Arrives']/span[@class='fldate']")).getText().replace(",", "") + " " + driver.findElement(By.xpath("//div[@class='fly5-flights fly5-depart th']/div[2]/div[" + i + "]/table/tbody/tr[@class='tr-link']/td[@data-title='Arrives']/span[@class='fltime ftop']")).getText();
            for (int j = 1; j <= listOfReturnFlights.size(); j++) {

                if (!departHasBeenClicked) {
                    driver.findElement(By.xpath("//div[@class='fly5-flights fly5-depart th']/div[2]/div[" + i + "]")).click();
                }
                departHasBeenClicked = false;
                driver.findElement(By.xpath("//div[@class='fly5-flights fly5-depart th']/div[2]/div[" + i + "]/div[1]/div[1]/div[" + cardIndexDepart + "]/div[1]/div[2]/button")).click();
                driver.findElement(By.xpath("//div[@class='fly5-flights fly5-return th']/div[2]/div[" + j + "]")).click();
                if (!driver.findElement(By.xpath("//div[@class='fly5-flights fly5-return th']/div[2]/div[" + j + "]/table/tbody/tr[@class='tr-link']/td[@class='fdetails fpackage fp1']/span[contains(@class,'flprice')]")).getText().equals("SOLD OUT")) {
                    returnCheapestPrice = Double.parseDouble(driver.findElement(By.xpath("//div[@class='fly5-flights fly5-return th']/div[2]/div[" + j + "]/table/tbody/tr[@class='tr-link']/td[@class='fdetails fpackage fp1']/span[@class='flprice']")).getText().replace(",", ""));
                } else {
                    continue;
                }
                int cardIndexReturn = 1;
                if (!findExistingPrice(j, cardIndexReturn, driver, "return")) {
                    cardIndexReturn++;
                    if (!findExistingPrice(j, cardIndexReturn, driver, "return")) {
                        cardIndexReturn++;
                        if (!findExistingPrice(j, cardIndexReturn, driver, "return")) {
                            System.out.println("Return flight sold out");
                            continue;
                        }
                    }
                }
                String returnDepTime = driver.findElement(By.xpath("//div[@class='fly5-flights fly5-return th']/div[2]/div[" + j + "]/table/tbody/tr[@class='tr-link']/td[@data-title='Departs']/span[@class='fldate']")).getText().replace(",", "") + " " + driver.findElement(By.xpath("//div[@class='fly5-flights fly5-return th']/div[2]/div[" + j + "]/table/tbody/tr[@class='tr-link']/td[@data-title='Departs']/span[@class='fltime ftop']")).getText();
                String returnArrTime = driver.findElement(By.xpath("//div[@class='fly5-flights fly5-return th']/div[2]/div[" + j + "]/table/tbody/tr[@class='tr-link']/td[@data-title='Arrives']/span[@class='fldate']")).getText().replace(",", "") + " " + driver.findElement(By.xpath("//div[@class='fly5-flights fly5-return th']/div[2]/div[" + j + "]/table/tbody/tr[@class='tr-link']/td[@data-title='Arrives']/span[@class='fltime ftop']")).getText();
                driver.findElement(By.xpath("//div[@class='fly5-flights fly5-return th']/div[2]/div[" + j + "]/div[1]/div[1]/div[1]/div[1]/div[2]/button")).click();
                driver.findElement(By.xpath("//*[@id=\"continue-btn\"]")).click();
                driver.findElement(By.xpath("/html/body/div[1]/div/section/div[2]/div/div/div[2]/div/div[4]/a")).click();
                totalTaxes += Double.parseDouble(driver.findElement(By.xpath("/html/body/div[1]/div/section/div[2]/div/div/div[2]/div/div[4]/div/div/div[1]/div[2]/span")).getText());    //forward taxes
                totalTaxes += Double.parseDouble(driver.findElement(By.xpath("/html/body/div[1]/div/section/div[2]/div/div/div[2]/div/div[4]/div/div/div[2]/div[2]/span")).getText());    //return taxes
                String[] roundFlight = new String[10];
                roundFlight[0] = departureDepAirport;
                roundFlight[1] = departureArrAirport;
                roundFlight[2] = departureDepTime;
                roundFlight[3] = departureArrTime;
                roundFlight[4] = returnDepAirport;
                roundFlight[5] = returnArrAirport;
                roundFlight[6] = returnDepTime;
                roundFlight[7] = returnArrTime;
                roundFlight[8] = (departureCheapestPrice + returnCheapestPrice) + "";
                roundFlight[9] = totalTaxes + "";
                if (departureCheapestPrice == 0 || returnCheapestPrice == 0) {
                    roundFlight[8] = (0 + "");
                } else {
                    roundFlight[8] = ((departureCheapestPrice + returnCheapestPrice) + "");
                }
                listOfFlights.add(roundFlight);
                totalTaxes = 0;
                driver.navigate().back();
            }
        }

        return listOfFlights;
    }

    private void writeToFile(List<String[]> listOfFlights) {
        String fileName = "Flights" + java.time.LocalTime.now().getNano() + ".csv";
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter outputFile = new FileWriter(file);
            CSVWriter writer = new CSVWriter(outputFile, ';',
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END);
            String[] roundFlightInfo = new String[10];
            roundFlightInfo[0] = "outbound_departure_airport";
            roundFlightInfo[1] = "outbound_arrival_airport";
            roundFlightInfo[2] = "outbound_departure_time";
            roundFlightInfo[3] = "outbound_arrival_time";
            roundFlightInfo[4] = "inbound_departure_airport";
            roundFlightInfo[5] = "inbound_arrival_airport";
            roundFlightInfo[6] = "inbound_departure_time";
            roundFlightInfo[7] = "inbound_arrival_time";
            roundFlightInfo[8] = "total_price";
            roundFlightInfo[9] = "taxes";
            writer.writeNext(roundFlightInfo);
            writer.writeAll(listOfFlights);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
