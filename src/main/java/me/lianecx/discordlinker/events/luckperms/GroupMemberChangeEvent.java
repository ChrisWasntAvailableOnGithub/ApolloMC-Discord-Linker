package me.lianecx.discordlinker.events.luckperms;

import me.lianecx.discordlinker.DiscordLinker;
import net.luckperms.api.event.node.NodeMutateEvent;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;

import java.util.Set;
import java.util.stream.Collectors;

import static org.bukkit.Bukkit.getScheduler;

public class GroupMemberChangeEvent {

    public static void onNodeMutate(NodeMutateEvent event) {
        System.out.println(event);
        if(!event.isUser()) return;

        // Check if the node was an inheritance node
        Set<InheritanceNode> addedInheritance = event.getDataAfter().stream()
                .filter(node -> node.getType() == NodeType.INHERITANCE && !event.getDataBefore().contains(node))
                .map(NodeType.INHERITANCE::cast)
                .collect(Collectors.toSet());

        Set<InheritanceNode> removedInheritance = event.getDataBefore().stream()
                .filter(node -> node.getType() == NodeType.INHERITANCE && !event.getDataAfter().contains(node))
                .map(NodeType.INHERITANCE::cast)
                .collect(Collectors.toSet());
        if(addedInheritance.isEmpty() && removedInheritance.isEmpty()) return;

        addedInheritance.forEach(node -> sendRoleSyncUpdateFromGroup(event.getLuckPerms().getGroupManager().getGroup(node.getGroupName())));
        removedInheritance.forEach(node -> sendRoleSyncUpdateFromGroup(event.getLuckPerms().getGroupManager().getGroup(node.getGroupName())));
    }

    private static void sendRoleSyncUpdateFromGroup(Group group) {
        if(group == null) return;
        getScheduler().runTaskLater(DiscordLinker.getPlugin(), () ->
                DiscordLinker.getAdapterManager().updateSyncedRole(group.getName(), true), 20);
    }
}
