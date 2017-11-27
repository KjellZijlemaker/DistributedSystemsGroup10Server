package distributed.systems.das.server;

/**
 * Different request types for the
 * nodes to send to the server.
 * 
 * @author Pieter Anemaet, Boaz Pat-El
 */
public enum MessageRequest {
	spawnUnit, getUnit, moveUnit, putUnit, removeUnit, getType, dealDamage, healDamage,
}