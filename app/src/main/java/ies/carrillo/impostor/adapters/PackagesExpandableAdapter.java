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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ies.carrillo.impostor.R;
import ies.carrillo.impostor.model.Categoria;

public class PackagesExpandableAdapter extends BaseExpandableListAdapter {

    private final Context context;
    private List<String> groupNames;
    private HashMap<String, List<Categoria>> groupChildren;
    private List<Categoria> selectedCategories; // Referencia a la lista de categorías seleccionadas
    private OnPackageActionListener listener;

    // --- MAPA DE ICONOS ---
    // Asociamos el nombre de la categoría/grupo con su ID de recurso (Drawable ID)
    private static final Map<String, Integer> ICON_MAP = new HashMap<>();

    static {
        // Asegúrate de que estos nombres coincidan exactamente con los de la DataBase
        ICON_MAP.put("Semana Santa", R.drawable.semanasanta_icon);
        ICON_MAP.put("Naturaleza", R.drawable.nature);
        ICON_MAP.put("Personalizadas", R.drawable.category_custom);
    }
    // ----------------------

    // Interfaz para manejar clics fuera del adaptador
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
    }

    /**
     * Permite actualizar la lista de hijos (necesario cuando se crea o elimina un paquete personalizado).
     */
    public void updateData(HashMap<String, List<Categoria>> newChildren) {
        this.groupChildren = newChildren;
        notifyDataSetChanged();
    }

    /**
     * Método auxiliar para obtener la posición de un grupo por su nombre.
     */
    public int getGroupPosition(String groupName) {
        return groupNames.indexOf(groupName);
    }


    // --- BASE EXPANDABLE ADAPTER IMPLEMENTATION ---

    @Override
    public int getGroupCount() {
        return groupNames.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        String groupName = groupNames.get(groupPosition);
        List<Categoria> children = groupChildren.get(groupName);

        // Si es el grupo "Personalizadas", añadimos 1 extra para el botón de "Crear Nuevo Paquete"
        if ("Personalizadas".equals(groupName)) {
            return (children != null ? children.size() : 0) + 1;
        }

        // Los grupos predefinidos no tienen hijos visibles
        return 0;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groupNames.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        String groupName = groupNames.get(groupPosition);
        List<Categoria> children = groupChildren.get(groupName);

        if ("Personalizadas".equals(groupName) && childPosition < (children != null ? children.size() : 0)) {
            return children.get(childPosition);
        }
        return null; // El último elemento es el botón de 'Añadir nuevo'
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    // --- GRUPO VIEW (CATEGORÍA PREDEFINIDA O GRUPO PERSONALIZADO) ---

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_group_package, parent, false);
        }

        final String groupName = (String) getGroup(groupPosition);

        TextView tvGroupName = convertView.findViewById(R.id.tv_package_name);
        CheckBox cbGroupCheck = convertView.findViewById(R.id.cb_package_select);
        ImageView ivGroupIcon = convertView.findViewById(R.id.img_package_icon);

        tvGroupName.setText(groupName);

        // 1. Lógica de Iconos
        if (ICON_MAP.containsKey(groupName)) {
            ivGroupIcon.setVisibility(View.VISIBLE);
            ivGroupIcon.setImageResource(ICON_MAP.get(groupName));
        } else {
            ivGroupIcon.setVisibility(View.GONE);
        }

        // 2. Lógica de Checkbox
        if ("Personalizadas".equals(groupName)) {
            // El grupo 'Personalizadas' no es seleccionable.
            cbGroupCheck.setVisibility(View.GONE);
        } else {
            // Grupos Predefinidos (Semana Santa, Naturaleza) son seleccionables.
            cbGroupCheck.setVisibility(View.VISIBLE);

            // Comprobamos si la categoría predefinida está seleccionada en la lista principal
            boolean isChecked = selectedCategories.stream()
                    .anyMatch(c -> c.getName().equals(groupName));
            cbGroupCheck.setChecked(isChecked);

            cbGroupCheck.setOnClickListener(v -> {
                listener.onGroupCheckboxToggled(groupName, cbGroupCheck.isChecked());
            });
        }

        return convertView;
    }

    // --- CHILD VIEW (PAQUETE PERSONALIZADO INDIVIDUAL O BOTÓN AÑADIR) ---

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final String groupName = (String) getGroup(groupPosition);

        // El último elemento del grupo "Personalizadas" es el botón de añadir nuevo.
        if ("Personalizadas".equals(groupName) && childPosition == getChildrenCount(groupPosition) - 1) {

            // Inflar la vista del botón de añadir
            convertView = LayoutInflater.from(context).inflate(R.layout.list_child_add_package, parent, false);

            convertView.setOnClickListener(v -> listener.onAddNewPackageClicked());

            return convertView;
        }

        // Los otros elementos son categorías personalizadas
        final Categoria category = (Categoria) getChild(groupPosition, childPosition);

        if (convertView == null || convertView.getTag() == null || !(convertView.getTag() instanceof ViewHolder)) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_child_custom_package, parent, false);
            ViewHolder holder = new ViewHolder();
            holder.tvChildName = convertView.findViewById(R.id.tv_child_package_name);
            holder.cbChildCheck = convertView.findViewById(R.id.cb_child_select);
            holder.btnEdit = convertView.findViewById(R.id.btn_edit_package);
            holder.btnDelete = convertView.findViewById(R.id.btn_delete_package);
            convertView.setTag(holder);
        }

        final ViewHolder holder = (ViewHolder) convertView.getTag();

        holder.tvChildName.setText(category.getName());

        // Lógica de Checkbox (selección)
        boolean isChecked = selectedCategories.contains(category);
        holder.cbChildCheck.setChecked(isChecked);

        holder.cbChildCheck.setOnClickListener(v -> {
            listener.onChildCheckboxToggled(category, holder.cbChildCheck.isChecked());
        });

        // Lógica de Botones de Acción
        holder.btnEdit.setOnClickListener(v -> listener.onEditPackageClicked(category));
        holder.btnDelete.setOnClickListener(v -> listener.onDeletePackageClicked(category));

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    // Patrón ViewHolder para optimizar la carga de vistas hijo
    private static class ViewHolder {
        TextView tvChildName;
        CheckBox cbChildCheck;
        ImageButton btnEdit;
        ImageButton btnDelete;
    }
}