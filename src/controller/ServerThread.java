/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import dao.UserDAO;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.User;

/**
 *
 * @author Admin
 */
public class ServerThread implements Runnable {
    
    private User user;
    private Socket socketOfServer;
    private int clientNumber;
    private BufferedReader is;
    private BufferedWriter os;
    private boolean isClosed;
    private Room room;
    private UserDAO userDAO;
    private String clientIP;
    
    public BufferedReader getIs() {
        return is;
    }
    
    public BufferedWriter getOs() {
        return os;
    }

    public void setRoom(Room room) {
        this.room = room;
    }
    
    public int getClientNumber() {
        return clientNumber;
    }

    public User getUser() {
        return user;
    }


    public Room getRoom() {
        return room;
    }

    public String getClientIP() {
        return clientIP;
    }

    public void setUser(User user) {
        this.user = user;
    }
    
    public ServerThread(Socket socketOfServer, int clientNumber) {
        this.socketOfServer = socketOfServer;
        this.clientNumber = clientNumber;
        System.out.println("Server thread number " + clientNumber + " Started");
        userDAO = new UserDAO();
        isClosed = false;
        room = null;
        //Trường hợp test máy ở server sẽ lỗi do hostaddress là localhost
        if(this.socketOfServer.getInetAddress().getHostAddress().equals("127.0.0.1")){
            clientIP = "127.0.0.1";
        }
        else{
            clientIP = this.socketOfServer.getInetAddress().getHostAddress();
        }
        
    }
    public String getStringFromUser(User user1){
        return ""+user1.getID()+","+user1.getUsername()
                                +","+user1.getPassword()+","+user1.getNickname()+","+
                                user1.getAvatar()+","+user1.getNumberOfGame()+","+
                                user1.getNumberOfwin()+","+user1.getNumberOfDraw()+","+user1.getRank();
    }
    
    public void goToOwnRoom() throws IOException{
        write("go-to-room," + room.getID()+","+room.getCompetitor(this.getClientNumber()).getClientIP()+",1,"+getStringFromUser(room.getCompetitor(this.getClientNumber()).getUser()));
        room.getCompetitor(this.clientNumber).write("go-to-room," + room.getID()+","+this.clientIP+",0,"+getStringFromUser(user));
    }
    
    public void goToPartnerRoom() throws IOException{
        write("go-to-room," + room.getID()+","+room.getCompetitor(this.getClientNumber()).getClientIP()+",0,"+getStringFromUser(room.getCompetitor(this.getClientNumber()).getUser()));
         room.getCompetitor(this.clientNumber).write("go-to-room,"+ room.getID()+","+this.clientIP+",1,"+getStringFromUser(user));
    }
    
    @Override
    public void run() {
        try {
            // Mở luồng vào ra trên Socket tại Server.
            is = new BufferedReader(new InputStreamReader(socketOfServer.getInputStream()));
            os = new BufferedWriter(new OutputStreamWriter(socketOfServer.getOutputStream()));
            System.out.println("Khời động luông mới thành công, ID là: " + clientNumber);
            write("server-send-id" + "," + this.clientNumber);
            String message;
            while (!isClosed) {
                message = is.readLine();
                if (message == null) {
                    break;
                }
                String[] messageSplit = message.split(",");
                //Xác minh
                if(messageSplit[0].equals("client-verify")){
                    System.out.println(message);
                    User user1 = userDAO.verifyUser(new User(messageSplit[1], messageSplit[2]));
                    if(user1==null)
                        write("wrong-user,"+messageSplit[1]+","+messageSplit[2]);
                    else if(!user1.getIsOnline()&&!userDAO.checkIsBanned(user1)){
                        write("login-success,"+getStringFromUser(user1));
                        this.user = user1;
                        userDAO.updateToOnline(this.user.getID());
                        Server.serverThreadBus.boardCast(clientNumber, "chat-server,"+user1.getNickname()+" đang online");
                        Server.admin.addMessage("["+user1.getID()+"] "+user1.getNickname()+ " đang online");
                    } else if(!userDAO.checkIsBanned(user1)){
                        write("dupplicate-login,"+messageSplit[1]+","+messageSplit[2]);
                    }
                    else{
                        write("banned-user,"+messageSplit[1]+","+messageSplit[2]);
                    }
                }
                //Xử lý đăng kí
                if(messageSplit[0].equals("register")){
                   boolean checkdup = userDAO.checkDuplicated(messageSplit[1]);
                   if(checkdup) write("duplicate-username,");
                   else{
                       User userRegister = new User(messageSplit[1], messageSplit[2], messageSplit[3], messageSplit[4]);
                       userDAO.addUser(userRegister);
                       User userRegistered = userDAO.verifyUser(userRegister);
                       this.user = userRegistered;
                       userDAO.updateToOnline(this.user.getID());
                       Server.serverThreadBus.boardCast(clientNumber, "chat-server,"+this.user.getNickname()+" đang online");
                       write("login-success,"+getStringFromUser(this.user));
                   }
                }
                //Xử lý người chơi đăng xuất
                if(messageSplit[0].equals("offline")){
                    userDAO.updateToOffline(this.user.getID());
                    Server.admin.addMessage("["+user.getID()+"] "+user.getNickname()+" đã offline");
                    Server.serverThreadBus.boardCast(clientNumber, "chat-server,"+this.user.getNickname()+" đã offline");
                    this.user=null;
                }
                //Xử lý xem danh sách bạn bè
                if(messageSplit[0].equals("view-friend-list")){
                    List<User> friends = userDAO.getListFriend(this.user.getID());
                    String res = "return-friend-list,";
                    for(User friend : friends){
                        res += friend.getID() + "," + friend.getNickname()+"," + (friend.getIsOnline()==true?1:0) +"," + (friend.getIsPlaying()==true?1:0)+",";
                    }
                    System.out.println(res);
                    write(res);
                }
                //Xử lý chat toàn server
                if(messageSplit[0].equals("chat-server")){
                    Server.serverThreadBus.boardCast(clientNumber,messageSplit[0]+","+ user.getNickname()+" : "+ messageSplit[1]);
                    Server.admin.addMessage("["+user.getID()+"] "+user.getNickname()+" : "+ messageSplit[1]);
                }
                //Xử lý vào phòng trong chức năng tìm kiếm phòng
                if(messageSplit[0].equals("go-to-room")){
                    int roomName = Integer.parseInt(messageSplit[1]);
                    boolean isFinded = false;
                    for (ServerThread serverThread : Server.serverThreadBus.getListServerThreads()) {
                        if(serverThread.getRoom()!=null&&serverThread.getRoom().getID()==roomName){
                            isFinded = true;
                            if(serverThread.getRoom().getNumberOfUser()==2){
                                write("room-fully,");
                            }
                            else{
                                if(serverThread.getRoom().getPassword()==null||serverThread.getRoom().getPassword().equals(messageSplit[2])){
                                    this.room = serverThread.getRoom();
                                    room.setUser2(this);
                                    room.increaseNumberOfGame();
                                    this.userDAO.updateToPlaying(this.user.getID());
                                    goToPartnerRoom();
                                }
                                else{
                                    write("room-wrong-password,");
                                }
                            }
                            break;
                        }
                    }
                    if(!isFinded){
                        write("room-not-found,");
                    }
                }
                //Xử lý lấy danh sách bảng xếp hạng
                if(messageSplit[0].equals("get-rank-charts")){
                    List<User> ranks = userDAO.getUserStaticRank();
                    String res = "return-get-rank-charts,";
                    for(User user : ranks){
                        res += getStringFromUser(user)+",";
                    }
                    System.out.println(res);
                    write(res);
                }
                //Xử lý tạo phòng
                if (messageSplit[0].equals("create-room")) {
                    room = new Room(this);
                    if (messageSplit.length == 2) {
                        room.setPassword(messageSplit[1]);
                        write("your-created-room," + room.getID() + "," + messageSplit[1]);
                        System.out.println("Tạo phòng mới thành công, password là " + messageSplit[1]);
                    } else {
                        write("your-created-room," + room.getID());
                        System.out.println("Tạo phòng mới thành công");
                    } 
                    userDAO.updateToPlaying(this.user.getID());
                }
                //Xử lý xem danh sách phòng trống
                if (messageSplit[0].equals("view-room-list")) {
                    String res = "room-list,";
                    int number = 1;
                    for (ServerThread serverThread : Server.serverThreadBus.getListServerThreads()) {
                        if(number>8) break;
                        if (serverThread.room != null && serverThread.room.getNumberOfUser() == 1) {
                            res += serverThread.room.getID() + "," + serverThread.room.getPassword() + ",";
                        }
                        number++;
                    }
                    write(res);
                    System.out.println(res);
                }
                //Xử lý lấy thông tin kết bạn và rank
                if(messageSplit[0].equals("check-friend")){
                    String res = "check-friend-response,";
                    res += (userDAO.checkIsFriend(this.user.getID(), Integer.parseInt(messageSplit[1]))?1:0);
                    write(res);
                }
                //Xử lý tìm phòng nhanh
                if (messageSplit[0].equals("quick-room")) {
                    boolean isFinded = false;
                    for (ServerThread serverThread : Server.serverThreadBus.getListServerThreads()) {
                        if (serverThread.room != null && serverThread.room.getNumberOfUser() == 1 && serverThread.room.getPassword().equals(" ")) {
                            serverThread.room.setUser2(this);
                            this.room = serverThread.room;
                            room.increaseNumberOfGame();
                            System.out.println("Đã vào phòng " + room.getID());
                            goToPartnerRoom();
                            userDAO.updateToPlaying(this.user.getID());
                            isFinded = true;
                            //Xử lý phần mời cả 2 người chơi vào phòng
                            break;
                        }
                    }
                    
                    if (!isFinded) {
                        this.room = new Room(this);
                        userDAO.updateToPlaying(this.user.getID());
                        System.out.println("Không tìm thấy phòng, tạo phòng mới");
                    }
                }
                //Xử lý không tìm được phòng
                if (messageSplit[0].equals("cancel-room")) {
                    userDAO.updateToNotPlaying(this.user.getID());
                    System.out.println("Đã hủy phòng");
                    this.room = null;
                }
                //Xử lý khi có người chơi thứ 2 vào phòng
                if (messageSplit[0].equals("join-room")) {
                    int ID_room = Integer.parseInt(messageSplit[1]);
                    for (ServerThread serverThread : Server.serverThreadBus.getListServerThreads()) {
                        if (serverThread.room != null && serverThread.room.getID() == ID_room) {
                            serverThread.room.setUser2(this);
                            this.room = serverThread.room;
                            System.out.println("Đã vào phòng " + room.getID());
                            room.increaseNumberOfGame();
                            goToPartnerRoom();
                            userDAO.updateToPlaying(this.user.getID());
                            break;
                        }
                    }
                }
                //Xử lý yêu cầu kết bạn
                if (messageSplit[0].equals("make-friend")){
                    Server.serverThreadBus.getServerThreadByUserID(Integer.parseInt(messageSplit[1]))
                            .write("make-friend-request,"+this.user.getID()+","+userDAO.getNickNameByID(this.user.getID()));
                }
                //Xử lý xác nhận kết bạn
                if(messageSplit[0].equals("make-friend-confirm")){
                    userDAO.makeFriend(this.user.getID(), Integer.parseInt(messageSplit[1]));
                    System.out.println("Kết bạn thành công");
                }
                //Xử lý khi gửi yêu cầu thách đấu tới bạn bè
                if(messageSplit[0].equals("duel-request")){
                    Server.serverThreadBus.sendMessageToUserID(Integer.parseInt(messageSplit[1]),
                            "duel-notice,"+this.user.getID()+","+this.user.getNickname());
                }
                //Xử lý khi đối thủ đồng ý thách đấu
                if(messageSplit[0].equals("agree-duel")){
                    this.room = new Room(this);
                    int ID_User2 = Integer.parseInt(messageSplit[1]);
                    ServerThread user2 = Server.serverThreadBus.getServerThreadByUserID(ID_User2);
                    room.setUser2(user2);
                    user2.setRoom(room);
                    room.increaseNumberOfGame();
                    goToOwnRoom();
                    userDAO.updateToPlaying(this.user.getID());
                }
                //Xử lý khi không đồng ý thách đấu
                if(messageSplit[0].equals("disagree-duel")){
                    Server.serverThreadBus.sendMessageToUserID(Integer.parseInt(messageSplit[1]),message);
                }
                //Xử lý khi người chơi đánh 1 nước
                if(messageSplit[0].equals("caro")){
                    room.getCompetitor(clientNumber).write(message);
                }
                if(messageSplit[0].equals("chat")){
                    room.getCompetitor(clientNumber).write(message);
                }
                if(messageSplit[0].equals("win")){
                    userDAO.addWinGame(this.user.getID());
                    room.increaseNumberOfGame();
                    room.getCompetitor(clientNumber).write("caro,"+messageSplit[1]+","+messageSplit[2]);
                    room.boardCast("new-game,");
                }
                if(messageSplit[0].equals("lose")){
                    userDAO.addWinGame(room.getCompetitor(clientNumber).user.getID());
                    room.increaseNumberOfGame();
                    room.getCompetitor(clientNumber).write("competitor-time-out");
                    write("new-game,");
                }
                if(messageSplit[0].equals("draw-request")){
                    room.getCompetitor(clientNumber).write(message);
                }
                if(messageSplit[0].equals("draw-confirm")){
                    room.increaseNumberOfDraw();
                    room.increaseNumberOfGame();
                    room.boardCast("draw-game,");
                }
                if(messageSplit[0].equals("draw-refuse")){
                    room.getCompetitor(clientNumber).write("draw-refuse,");
                }
                if(messageSplit[0].equals("voice-message")){
                    room.getCompetitor(clientNumber).write(message);
                }
                if(messageSplit[0].equals("left-room")){
                    if (room != null) {
                        room.setUsersToNotPlaying();
                        room.decreaseNumberOfGame();
                        room.getCompetitor(clientNumber).write("left-room,");
                        room.getCompetitor(clientNumber).room = null;
                        this.room = null;
                    }
                }
            }
        } catch (IOException e) {
            //Thay đổi giá trị cờ để thoát luồng
            isClosed = true;
            //Cập nhật trạng thái của user
            if(this.user!=null){
                userDAO.updateToOffline(this.user.getID());
                userDAO.updateToNotPlaying(this.user.getID());
                Server.serverThreadBus.boardCast(clientNumber, "chat-server,"+this.user.getNickname()+" đã offline");
                Server.admin.addMessage("["+user.getID()+"] "+user.getNickname()+" đã offline");
            }
            
            //remove thread khỏi bus
            Server.serverThreadBus.remove(clientNumber);
            System.out.println(this.clientNumber + " đã thoát");
            if (room != null) {
                try {
                    if (room.getCompetitor(clientNumber) != null) {
                        room.decreaseNumberOfGame();
                        room.getCompetitor(clientNumber).write("left-room,");
                        room.getCompetitor(clientNumber).room = null;
                    }
                    this.room = null;
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

        }
    }

    public void write(String message) throws IOException {
        os.write(message);
        os.newLine();
        os.flush();
    }
}
