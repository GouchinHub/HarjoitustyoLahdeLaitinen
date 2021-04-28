package com.example.harjoitustyo;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.regex.Pattern;

public class LoginManager {

    private static final Random random = new SecureRandom();
    ArrayList<User> users = new ArrayList<>();
    private User activeUser = null;

    //Creates Singleton
    private static final LoginManager loginManager = new LoginManager();

    private LoginManager() {
    }

    public static LoginManager getInstance() {
        return loginManager;
    }

    //Generate random quest username and create guest user object. Quest users are not stored to shared preferences.
    //On logout and application shutdown, all guest user data is wiped.
    public void createGuestAccount() {
        String guestName = "User@";
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int i = 1; i < 101; i++) {
            list.add(i);
        }
        Collections.shuffle(list);
        for (int i = 0; i < 4; i++) {
            guestName += list.get(i);
        }
        System.out.println(guestName);
        //Guest users don't have password, and quest user flag attribute is set to true.
        activeUser = new User(guestName, null, null, true, 0);
        activeUser.getProfile().setEmail("Not available as guest");
    }

    public void userLogout() {
        activeUser = null;
    }

    //Method saves the view that is active for user.
    public void changeUserViewState(int viewState) {
        for (User user : users) {
            if (user.getName().matches(activeUser.getName())) {
                user.setViewState(viewState);
                activeUser = user;
                break;
            }
        }
    }

    //Method tests that given username and password are acceptable, and creates new account.
    //Method returns feedback string to inform user.
    //Username must not be taken, have no special characters and be at least 5 characters long.
    //Password must be at least 12 characters long and include at least one uppercase, lowercase, number, and special character.
    public String createAccount(String username, String password, String pwConfirm) {
        Pattern upperCasePattern = Pattern.compile("[A-Z]");
        Pattern lowerCasePattern = Pattern.compile("[a-z]");
        Pattern digitPattern = Pattern.compile("[0-9]");
        Pattern specialCasePattern = Pattern.compile("[^a-z0-9]", Pattern.CASE_INSENSITIVE);
        if (specialCasePattern.matcher(username).find()) {
            return "Username can't contain special characters";
        }
        if (username.length() < 5) {
            return "Username must be at least 5 characters long";
        }
        for (User user : users) {
            if (username.matches(user.getName())) {
                return "username taken";
            }
        }
        if (!password.matches(pwConfirm)) {
            return "Passwords don't match";
        }
        if (password.length() < 12) {
            return "Password must be at least 12 characters long";
        }
        if (!upperCasePattern.matcher(password).find()) {
            return "Password must contain upper case character";
        }
        if (!lowerCasePattern.matcher(password).find()) {
            return "Password must contain lower case character";
        }
        if (!digitPattern.matcher(password).find()) {
            return "Password must contain a digit";
        }
        if (!specialCasePattern.matcher(password).find()) {
            return "Password must contain a special character";
        }
        byte[] salt = getNextSalt();
        String hashedPassword = doHashing(password, salt);
        if (hashedPassword == null) {
            return "something went wrong";
        } else {
            //For regular users the guest user flag attribute is always set to false.
            users.add(new User(username, hashedPassword, salt, false, 0));
            return "User Created";
        }
    }

    //method gets list (fetched from shared preferences) as parameter and sets users ArrayList as that list.
    public void loadUsers(ArrayList<User> loadedList) {
        users = loadedList;
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    //generate new salt for user password
    @NotNull
    private static byte[] getNextSalt() {
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        System.out.println(salt.toString());
        return salt;
    }

    //Digests the user password and salt into SHA-512 hash, and returns hashed password as string
    @Nullable
    private static String doHashing(String password, byte[] salt) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
            messageDigest.update(salt);
            byte[] bytes = messageDigest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder stringBuilder = new StringBuilder();
            for (byte aByte : bytes) {
                stringBuilder.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
            }
            String hashedPassword = stringBuilder.toString();
            System.out.println(hashedPassword);
            return hashedPassword;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    //Method gets username and password as parameters.
    //Searches for username match from saved users, hashes the given password, and sees if it matches the stored password for that user
    //If login credentials match, set user as activeUser and return user
    @Nullable
    public User login(String username, String password) {
        for (User user : users) {
            if (username.matches(user.getName())) {
                String hashedPassword = doHashing(password, user.getSalt());
                if (hashedPassword.matches(user.getPassword())) {
                    activeUser = user;
                    return user;
                }
            }
        }
        return null;
    }

    public User getActiveUser() {
        return activeUser;
    }

    public void setActiveUser(User user) {
        activeUser = user;
    }

    //Method gets active profile data as parameter.
    //Search all users for matching user and update Profile attribute to new one
    public void saveUserProfileChanges(Profile activeProfile) {
        for (User user : users) {
            if (user.getName().matches(activeUser.getName())) {
                user.saveProfile(activeProfile);
                activeUser = user;
                break;
            }
        }
    }

}
