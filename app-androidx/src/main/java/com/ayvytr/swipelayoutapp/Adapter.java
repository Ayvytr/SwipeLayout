package com.ayvytr.swipelayoutapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ayvytr.swipelayout.SwipeLayout;

import androidx.recyclerview.widget.RecyclerView;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        private final int COUNT = 30;
        private final int[] itemsOffset = new int[COUNT];

        @Override
        public int getItemViewType(int position) {
            return position % 3;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            int layoutId;
            switch (viewType) {
                case 0:
                    layoutId = R.layout.item_type_0;
                    break;

                case 1:
                    layoutId = R.layout.item_type_0;
                    break;

                case 2:
                    layoutId = R.layout.item_type_0;
                    break;

                default:
                    throw new IllegalArgumentException();
            }
            View itemView = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
            final ViewHolder viewHolder = new ViewHolder(itemView);

            View.OnClickListener onClick = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewHolder.swipeLayout.animateClose();
                }
            };

            if (viewHolder.leftView != null) {
                viewHolder.leftView.setClickable(true);
                viewHolder.leftView.setOnClickListener(onClick);
            }

            if (viewHolder.rightView != null) {
                viewHolder.rightView.setClickable(true);
                viewHolder.rightView.setOnClickListener(onClick);
            }

//            viewHolder.swipeLayout.setOnSwipeListener(new SwipeLayout.OnSwipeListener() {
//                @Override
//                public void onBeginSwipe(SwipeLayout swipeLayout, boolean moveToRight) {
//                }
//
//                @Override
//                public void onSwipeClampReached(SwipeLayout swipeLayout, boolean moveToRight) {
//                    Toast.makeText(swipeLayout.getContext(),
//                            (moveToRight ? "Left" : "Right") + " clamp reached",
//                            Toast.LENGTH_SHORT)
//                            .show();
//                    viewHolder.textViewPos.setText("TADA!");
//                }
//
//                @Override
//                public void onLeftStickyEdge(SwipeLayout swipeLayout, boolean moveToRight) {
//                }
//
//                @Override
//                public void onRightStickyEdge(SwipeLayout swipeLayout, boolean moveToRight) {
//                }
//            });

            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
//            holder.textViewPos.setText("#" + (position + 1));
            holder.swipeLayout.setOffset(itemsOffset[position]);
        }

        @Override
        public void onViewDetachedFromWindow(ViewHolder holder) {
            if (holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
                itemsOffset[holder.getAdapterPosition()] = holder.swipeLayout.getOffset();
            }
        }

        @Override
        public void onViewRecycled(ViewHolder holder) {
            super.onViewRecycled(holder);
        }

        @Override
        public int getItemCount() {
            return COUNT;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {

//            private final TextView textViewPos;
            private final SwipeLayout swipeLayout;
            private final View rightView;
            private final View leftView;
            private final TextView tv;

            ViewHolder(View itemView) {
                super(itemView);
//                textViewPos = itemView.findViewById(R.id.text_view_pos);
                swipeLayout = itemView.findViewById(R.id.swipe_layout);
                rightView = itemView.findViewById(R.id.tv_right);
                leftView = itemView.findViewById(R.id.tv_left);
                tv = itemView.findViewById(R.id.tv);
            }
        }
    }