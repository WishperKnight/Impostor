package ies.carrillo.impostor.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.core.content.ContextCompat;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import ies.carrillo.impostor.R;
import ies.carrillo.impostor.model.Categoria;

/**
 * Adaptador para gestionar los paquetes (predefinidos y personalizados) en un ExpandableListView.
 */
public class PackagesExpandableAdapter extends BaseExpandableListAdapter {

    private final Context context;
    private final List<String> groupNames;
    private final HashMap<String, List<Categoria>> groupChildren;
    private final List<Categoria> selectedCategories;
    private final OnPackageActionListener listener;
    private final LayoutInflater layoutInflater;

    private final String PACKAGE_CUSTOM_GROUP = "Personalizadas";

    // Interfaz de callback (se mantiene igual)
    public interface OnPackageActionListener {
        void onGroupCheckboxToggled(String groupName, boolean isChecked);
        void onChildCheckboxToggled(Categoria category, boolean isChecked);
        void onEditPackageClicked(Categoria category);
        void onDeletePackageClicked(Categoria category);
        void onAddNewPackageClicked();
    }

    public PackagesExpandableAdapter(Context context, List<String> groupNames,
                                     HashMap<String, List<Categoria>> groupChildren,
                                     List<Categoria> selectedCategories,
                                     OnPackageActionListener listener) {
        this.context = context;
        this.groupNames = groupNames;
        this.groupChildren = groupChildren;
        this.selectedCategories = selectedCategories;
        this.listener = listener;
        this.layoutInflater = LayoutInflater.from(context);
    }

    // --- Patrón ViewHolder para Grupos (Headers) ---
    static class GroupViewHolder {
        TextView tvName;
        TextView tvStatus;
        CheckBox cbSelect;
        ImageView imgExpand;
    }

    // --- Patrón ViewHolder para Hijos (Custom Packages) ---
    static class ChildViewHolder {
        TextView tvName;
        CheckBox cbSelect;
        ImageButton btnEdit;
        ImageButton btnDelete;
    }

    // --- Métodos de Data ---

    @Override
    public int getGroupCount() {
        return groupNames.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        String groupName = groupNames.get(groupPosition);
        List<Categoria> children = groupChildren.get(groupName);
        if (groupName.equals(PACKAGE_CUSTOM_GROUP)) {
            // +1 para el botón de "Crear Nuevo"
            return children != null ? children.size() + 1 : 1;
        }
        return children != null ? children.size() : 0;
    }

    @Override public Object getGroup(int groupPosition) { return groupNames.get(groupPosition); }
    @Override public long getGroupId(int groupPosition) { return groupPosition; }
    @Override public boolean hasStableIds() { return false; }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        String groupName = groupNames.get(groupPosition);
        if (groupName.equals(PACKAGE_CUSTOM_GROUP)) {
            List<Categoria> customList = groupChildren.get(groupName);
            if (customList != null && childPosition < customList.size()) {
                return customList.get(childPosition);
            }
            return "ADD_NEW"; // Indicador especial
        }
        // Retornar un objeto real del grupo si es necesario (aunque los predefinidos no tienen hijos visibles)
        return Objects.requireNonNull(groupChildren.get(groupName)).get(childPosition);
    }

    @Override public long getChildId(int groupPosition, int childPosition) { return childPosition; }


    // --- Métodos de Vista (Renderizado) OPTIMIZADOS ---

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupViewHolder holder;
        String groupName = (String) getGroup(groupPosition);

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_group_package, parent, false);
            holder = new GroupViewHolder();
            holder.tvName = convertView.findViewById(R.id.tv_package_name);
            holder.tvStatus = convertView.findViewById(R.id.tv_package_status);
            holder.cbSelect = convertView.findViewById(R.id.cb_package_select);
            holder.imgExpand = convertView.findViewById(R.id.img_expand_indicator);
            convertView.setTag(holder);
        } else {
            holder = (GroupViewHolder) convertView.getTag();
        }

        holder.tvName.setText(groupName);

        // Lógica específica para el grupo de Personalizadas
        if (groupName.equals(PACKAGE_CUSTOM_GROUP)) {
            holder.cbSelect.setVisibility(View.GONE);
            holder.imgExpand.setVisibility(View.VISIBLE);
            holder.imgExpand.setImageResource(isExpanded ? R.drawable.ic_arrow_up : R.drawable.ic_arrow_down);

            int count = getChildrenCount(groupPosition) - 1;
            holder.tvStatus.setText("Gestiona y selecciona tus " + count + " paquetes.");

        } else { // Grupos predefinidos
            holder.cbSelect.setVisibility(View.VISIBLE);
            holder.imgExpand.setVisibility(View.GONE);

            // Desactivar listener antes de setear el estado
            holder.cbSelect.setOnClickListener(null);

            boolean isChecked = selectedCategories.stream()
                    .anyMatch(cat -> cat.getName().equals(groupName));
            holder.cbSelect.setChecked(isChecked);

            holder.tvStatus.setText(groupName.equals("Semana Santa") ? "Paquete predefinido de temas religiosos." :
                    "Paquete predefinido de temas ambientales y biológicos.");

            // Reasignar Listener
            holder.cbSelect.setOnClickListener(v -> {
                listener.onGroupCheckboxToggled(groupName, holder.cbSelect.isChecked());
            });
        }

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        String groupName = groupNames.get(groupPosition);

        // Si es la celda para crear un nuevo paquete
        if (groupName.equals(PACKAGE_CUSTOM_GROUP) && getChild(groupPosition, childPosition).equals("ADD_NEW")) {
            TextView btnAddNew = (TextView) layoutInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            btnAddNew.setText("+ CREAR NUEVO PAQUETE PERSONALIZADO");
            // Nota: Se asume que R.color.colorAccent existe en tu proyecto
            btnAddNew.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));

            // Convertir DP a PX para el padding
            float density = context.getResources().getDisplayMetrics().density;
            btnAddNew.setPadding((int) (32 * density), (int) (16 * density), (int) (16 * density), (int) (16 * density));

            btnAddNew.setOnClickListener(v -> listener.onAddNewPackageClicked());
            return btnAddNew;
        }

        // Es un paquete personalizado real
        final Categoria category = (Categoria) getChild(groupPosition, childPosition);
        ChildViewHolder holder;

        if (convertView == null || convertView.getTag() instanceof String) {
            convertView = layoutInflater.inflate(R.layout.list_child_custom_package, parent, false);
            holder = new ChildViewHolder();
            holder.tvName = convertView.findViewById(R.id.tv_child_package_name);
            holder.cbSelect = convertView.findViewById(R.id.cb_child_select);
            holder.btnEdit = convertView.findViewById(R.id.btn_edit_package);
            holder.btnDelete = convertView.findViewById(R.id.btn_delete_package);
            convertView.setTag(holder);
        } else {
            holder = (ChildViewHolder) convertView.getTag();
        }

        holder.tvName.setText(category.getName());

        // 1. Estado de Selección
        holder.cbSelect.setOnClickListener(null);
        boolean isChecked = selectedCategories.contains(category);
        holder.cbSelect.setChecked(isChecked);

        holder.cbSelect.setOnClickListener(v -> {
            listener.onChildCheckboxToggled(category, holder.cbSelect.isChecked());
        });

        // 2. Botón Editar
        holder.btnEdit.setOnClickListener(v -> listener.onEditPackageClicked(category));

        // 3. Botón Borrar
        holder.btnDelete.setOnClickListener(v -> listener.onDeletePackageClicked(category));

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    // Método para actualizar los datos
    public void updateData(HashMap<String, List<Categoria>> newChildren) {
        this.groupChildren.clear();
        this.groupChildren.putAll(newChildren);
        notifyDataSetChanged();
    }
}