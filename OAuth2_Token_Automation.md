# **OAuth2 Token Automation with Playwright**  

## **Problem**  
- Manual login and token extraction were required before triggering the ORP API suite.  
- Tokens were copied from browser developer tools and used as test data.  
- This was time-consuming and prone to errors.  

---

## **Solution (POC)**  
- Automated the token extraction using Playwright's Listener to capture the `Authorization` header.  
- Authenticated via Google login and passed the token to the API suite.  
- Reduced manual intervention and improved test efficiency.  

---

## **Tech Stack**  
- **Automation Tool:** Playwright  
- **Security Challenge:** Google blocking for excessive login attempts  
- **Authentication:** Google OAuth (suggested as a better alternative to OTP by Narasimhulu)  

---

## **Code Snippet (Crude but Effective)**  

```java
public void bypass_OAuth2() throws InterruptedException {
    try (Playwright playwright = Playwright.create()) {
        Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        Page page = browser.newPage();

        page.onRequest(request -> {
            if (request.url().equals("https://apigw.leadschool.in/ft-oms/v2/schools/summary?ay=25-26")) {
                String authorizationHeader = request.headers().get("authorization");
                if (authorizationHeader != null) {
                    System.out.println("Authorization Header: " + authorizationHeader);
                } else {
                    System.out.println("Authorization header not present in the request.");
                }
            }
        });

        page.navigate("https://ft-oc.leadschool.in/login");

        Locator googleSignInButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign in with Google"));
        Page popup = page.waitForPopup(() -> {
            googleSignInButton.click();
        });

        popup.getByLabel("Email or phone").fill("EMAIL");
        popup.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Next")).click();
        popup.getByLabel("Enter your password").fill("PASSWORD");
        popup.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Next")).click();

        Thread.sleep(10000);  // Adjust timeout as needed
        popup.close();
    }
}
