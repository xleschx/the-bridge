import java.util.*;
import java.io.*;
import java.math.*;

class Motorcycle {
    int x;
    int y;
    boolean isActive;

    public Motorcycle(int x, int y, boolean isActive) {
        this.x = x;
        this.y = y;
        this.isActive = isActive;
    }

    public Motorcycle(Motorcycle m) {
        this.x = m.x;
        this.y = m.y;
        this.isActive = m.isActive;
    }
}

class BridgeState {
    Motorcycle[] motorcycles;
    int speed;

    public BridgeState(Motorcycle[] motorcycles, int speed) {
        this.motorcycles = motorcycles;
        this.speed = speed;
    }

    public BridgeState(BridgeState state) {
        this.motorcycles = new Motorcycle[state.motorcycles.length];
        for (int i = 0; i < state.motorcycles.length; i++) {
            this.motorcycles[i] = new Motorcycle(state.motorcycles[i]);
        }
        this.speed = state.speed;
    }
}

interface Action {
    String getName();
    BridgeState execute(BridgeState state, String[] lanes);
}

class SPEED implements Action {
    public String getName() {
        return "SPEED";
    }

    public BridgeState execute(BridgeState state, String[] lanes) {
        BridgeState newState = new BridgeState(state);
        newState.speed++;
        for (Motorcycle m : newState.motorcycles) {
            if (m.isActive) {
                Player.move(m, newState.speed, lanes[m.y]);
            }
        }
        return newState;
    }
}

class SLOW implements Action {
    public String getName() {
        return "SLOW";
    }

    public BridgeState execute(BridgeState state, String[] lanes) {
        if (state.speed < 2) {
            return null;
        } else {
            BridgeState newState = new BridgeState(state);
            newState.speed--;
            for (Motorcycle m : newState.motorcycles) {
                if (m.isActive) {
                    Player.move(m, newState.speed, lanes[m.y]);
                }
            }
            return newState;
        }
    }
}

class UP implements Action {
    public String getName() {
        return "UP";
    }

    public BridgeState execute(BridgeState state, String[] lanes) {
        if (state.speed == 0) {
            return null;
        }
        for (Motorcycle m : state.motorcycles) {
            if (m.isActive && m.y == 0) {
                return null;
            }
        }

        BridgeState newState = new BridgeState(state);
        for (Motorcycle m : newState.motorcycles) {
            if (m.isActive) {
                if (Player.checkWillFall(m, newState.speed - 1, lanes[m.y])) {
                    m.isActive = false;
                } else {
                    m.y--;
                    Player.move(m, newState.speed, lanes[m.y]);
                }
            }
        }
        return newState;
    }
}

class DOWN implements Action {
    public String getName() {
        return "DOWN";
    }

    public BridgeState execute(BridgeState state, String[] lanes) {
        if (state.speed == 0) {
            return null;
        }
        for (Motorcycle m : state.motorcycles) {
            if (m.isActive && (m.y == 3)) {
                return null;
            }
        }

        BridgeState newState = new BridgeState(state);
        for (int i = 0; i < newState.motorcycles.length; i++) {
            Motorcycle m = newState.motorcycles[i];
            if (m.isActive) {
                if (Player.checkWillFall(m, newState.speed - 1, lanes[m.y])) {
                    m.isActive = false;
                } else {
                    m.y++;
                    Player.move(m, newState.speed, lanes[m.y]);
                }
            }
        }
        return newState;
    }
}

class JUMP implements Action {
    public String getName() {
        return "JUMP";
    }

    public BridgeState execute(BridgeState state, String[] lanes) {
        if (state.speed == 0) {
            return null;
        }

        BridgeState newState = new BridgeState(state);
        for (Motorcycle m : newState.motorcycles) {
            if (m.isActive) {
                if (m.x + newState.speed < lanes[m.y].length() && lanes[m.y].charAt(m.x + newState.speed) == '0') {
                    m.isActive = false;
                } else {
                    m.x += newState.speed;
                }
            }
        }
        return newState;
    }
}

class Player {
    static public boolean checkWillFall(Motorcycle m, int speed, String lane) {
        for (int x = m.x + 1; x <= Math.min(m.x + speed, lane.length() - 1); x++) {
            if (lane.charAt(x) == '0') {
                return true;
            }
        }
        return false;
    }

    static public void move(Motorcycle m, int speed, String lane) {
        if (checkWillFall(m, speed, lane)) {
            m.isActive = false;
        } else {
            m.x += speed;
        }
    }

    public static String determineOptimalMove(BridgeState state, int min, String[] lanes) {
        int activeMotorcyclesCount = 0;
        int x = 0;
        for (Motorcycle m : state.motorcycles) {
            if (m.isActive) {
                activeMotorcyclesCount++;
                x = m.x;
            }
        }
        if (activeMotorcyclesCount < min) {
            return null;
        }
        if (x >= lanes[0].length()) {
            return "WAIT";
        }

        Action[] actions = {new SPEED(), new JUMP(), new UP(), new DOWN(), new SLOW()};
        for (Action a : actions) {
            BridgeState newState = a.execute(state, lanes);
            if (newState != null && determineOptimalMove(newState, min, lanes) != null) {
                return a.getName();
            }
        }
        return null;
    }

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);

        int m = in.nextInt();
        int v = in.nextInt();
        in.nextLine();

        String[] lanes = new String[4];
        for (int i = 0; i < 4; i++) {
            lanes[i] = in.nextLine();
        }

        Motorcycle[] motorcycles = new Motorcycle[m];
        BridgeState state = null;

        while (true) {
            int speed = in.nextInt();
            in.nextLine();

            for (int i = 0; i < m; i++) {
                int x = in.nextInt();
                int y = in.nextInt();
                boolean isActive = (in.nextInt() == 1);
                in.nextLine();
                motorcycles[i] = new Motorcycle(x, y, isActive);
            }
            state = new BridgeState(motorcycles, speed);

            String move = determineOptimalMove(state, m, lanes);
            if (move != null) {
                System.out.println(move);
            } else {
                System.out.println(determineOptimalMove(state, v, lanes));
            }
        }
    }
}
