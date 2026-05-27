package learngl.tools.shape;

import org.joml.Vector3f;

public class PreVerticesTable {

    public static float[] generateCubeSimple(float n) {
        float h = n / 2f;

        return new float[]{
                -h, -h,  h,   h, -h,  h,   h,  h,  h,
                h,  h,  h,  -h,  h,  h,  -h, -h,  h,
                -h, -h, -h,  -h,  h, -h,   h,  h, -h,
                h,  h, -h,   h, -h, -h,  -h, -h, -h,
                -h, -h, -h,  -h, -h,  h,  -h,  h,  h,
                -h,  h,  h,  -h,  h, -h,  -h, -h, -h,
                h, -h, -h,   h,  h, -h,   h,  h,  h,
                h,  h,  h,   h, -h,  h,   h, -h, -h,
                -h,  h, -h,  -h,  h,  h,   h,  h,  h,
                h,  h,  h,   h,  h, -h,  -h,  h, -h,
                -h, -h, -h,   h, -h, -h,   h, -h,  h,
                h, -h,  h,  -h, -h,  h,  -h, -h, -h
        };
    }

    public static float[] generatePyramid(float baseSize, float height) {
        float h = baseSize / 2f;

        return new float[]{
                -h, 0, -h,   h, 0, -h,   h, 0,  h,
                h, 0,  h,  -h, 0,  h,  -h, 0, -h,

                -h, 0,  h,    h, 0,  h,    0, height, 0,
                -h, 0, -h,    0, height, 0,    h, 0, -h,
                -h, 0, -h,   -h, 0,  h,    0, height, 0,
                h, 0, -h,    h, 0,  h,    0, height, 0
        };
    }

    public static float[] generatePyramid(float sideLength) {
        float hTriangle = (float) (Math.sqrt(3) / 2 * sideLength);
        float height = (float) (Math.sqrt(2.0 / 3.0) * sideLength);

        Vector3f v0 = new Vector3f(-sideLength / 2, 0, -hTriangle / 3);
        Vector3f v1 = new Vector3f(sideLength / 2, 0, -hTriangle / 3);
        Vector3f v2 = new Vector3f(0, 0, 2 * hTriangle / 3);
        Vector3f apex = new Vector3f(0, height, 0);

        return new float[]{
                v0.x, v0.y, v0.z,
                v1.x, v1.y, v1.z,
                v2.x, v2.y, v2.z,

                v0.x, v0.y, v0.z,
                v1.x, v1.y, v1.z,
                apex.x, apex.y, apex.z,

                v1.x, v1.y, v1.z,
                v2.x, v2.y, v2.z,
                apex.x, apex.y, apex.z,

                v2.x, v2.y, v2.z,
                v0.x, v0.y, v0.z,
                apex.x, apex.y, apex.z
        };
    }

    public static float[] generatePlayerShip(float size) {
        float s = size;

        // ---- VERTICES -------------------------------------------------------
        // Fuselage: triangular cross-section (top, bottom-left, bottom-right)
        // Nose faces -Z, tail faces +Z.
        int
          NOSE = 0,                    // nose tip
          FS1_T = 1, FS1_BL = 2, FS1_BR = 3,   // front section (z = -0.40)
          FS2_T = 4, FS2_BL = 5, FS2_BR = 6,    // mid section   (z = -0.08)
          FS3_T = 7, FS3_BL = 8, FS3_BR = 9,    // rear-mid      (z = +0.15)
          FS4_T =10, FS4_BL=11, FS4_BR=12,       // rear          (z = +0.30)
          CAN_F =13, CAN_T =14, CAN_R =15,       // canopy
          LW_RF_T=16, LW_RF_B=17,                // left wing root front upper/lower
          LW_T_T =18, LW_T_B =19,                // left wing tip upper/lower
          LW_RR_T=20, LW_RR_B=21,                // left wing root rear upper/lower
          RW_RF_T=22, RW_RF_B=23,                // right wing root front upper/lower
          RW_T_T =24, RW_T_B =25,                // right wing tip upper/lower
          RW_RR_T=26, RW_RR_B=27,                // right wing root rear upper/lower
          ENG_L_F=28, ENG_L_B=29,                // left engine  front/rear
          ENG_R_F=30, ENG_R_B=31,                // right engine front/rear
          FIN_F  =32, FIN_T  =33, FIN_R  =34;    // vertical fin

        Vector3f[] v = new Vector3f[35];
        v[NOSE]    = new Vector3f(0,      0,      -s*0.55f);
        v[FS1_T]   = new Vector3f(0,      s*0.07f, -s*0.40f);
        v[FS1_BL]  = new Vector3f(-s*0.07f,-s*0.04f,-s*0.40f);
        v[FS1_BR]  = new Vector3f( s*0.07f,-s*0.04f,-s*0.40f);
        v[FS2_T]   = new Vector3f(0,      s*0.09f, -s*0.08f);
        v[FS2_BL]  = new Vector3f(-s*0.12f,-s*0.06f,-s*0.08f);
        v[FS2_BR]  = new Vector3f( s*0.12f,-s*0.06f,-s*0.08f);
        v[FS3_T]   = new Vector3f(0,      s*0.07f,  s*0.15f);
        v[FS3_BL]  = new Vector3f(-s*0.10f,-s*0.05f, s*0.15f);
        v[FS3_BR]  = new Vector3f( s*0.10f,-s*0.05f, s*0.15f);
        v[FS4_T]   = new Vector3f(0,      s*0.05f,  s*0.30f);
        v[FS4_BL]  = new Vector3f(-s*0.07f,-s*0.04f, s*0.30f);
        v[FS4_BR]  = new Vector3f( s*0.07f,-s*0.04f, s*0.30f);

        v[CAN_F]   = new Vector3f(0,      s*0.16f, -s*0.24f);
        v[CAN_T]   = new Vector3f(0,      s*0.20f, -s*0.08f);
        v[CAN_R]   = new Vector3f(0,      s*0.14f,  s*0.06f);

        v[LW_RF_T] = new Vector3f(-s*0.10f, s*0.02f, -s*0.05f);
        v[LW_RF_B] = new Vector3f(-s*0.10f,-s*0.02f, -s*0.05f);
        v[LW_T_T]  = new Vector3f(-s*0.40f, s*0.01f,  s*0.18f);
        v[LW_T_B]  = new Vector3f(-s*0.40f,-s*0.03f,  s*0.18f);
        v[LW_RR_T] = new Vector3f(-s*0.10f, s*0.02f,  s*0.25f);
        v[LW_RR_B] = new Vector3f(-s*0.10f,-s*0.02f,  s*0.25f);

        v[RW_RF_T] = new Vector3f( s*0.10f, s*0.02f, -s*0.05f);
        v[RW_RF_B] = new Vector3f( s*0.10f,-s*0.02f, -s*0.05f);
        v[RW_T_T]  = new Vector3f( s*0.40f, s*0.01f,  s*0.18f);
        v[RW_T_B]  = new Vector3f( s*0.40f,-s*0.03f,  s*0.18f);
        v[RW_RR_T] = new Vector3f( s*0.10f, s*0.02f,  s*0.25f);
        v[RW_RR_B] = new Vector3f( s*0.10f,-s*0.02f,  s*0.25f);

        v[ENG_L_F] = new Vector3f(-s*0.05f,-s*0.04f,  s*0.28f);
        v[ENG_L_B] = new Vector3f(-s*0.05f,-s*0.04f,  s*0.42f);
        v[ENG_R_F] = new Vector3f( s*0.05f,-s*0.04f,  s*0.28f);
        v[ENG_R_B] = new Vector3f( s*0.05f,-s*0.04f,  s*0.42f);

        v[FIN_F]   = new Vector3f(0,      s*0.12f,  s*0.05f);
        v[FIN_T]   = new Vector3f(0,      s*0.22f,  s*0.15f);
        v[FIN_R]   = new Vector3f(0,      s*0.08f,  s*0.28f);

        // ---- TRIANGLES ------------------------------------------------------
        // Each entry = {a, b, c, colorIndex}
        int[][] tris = {

            // --- Nose cone (indices 0-2) ------------------------------------
            {NOSE, FS1_T,  FS1_BL,  0},   // upper left
            {NOSE, FS1_BR, FS1_T,   0},   // upper right
            {NOSE, FS1_BL, FS1_BR,  1},   // lower

            // --- Fuselage section FS1→FS2 (indices 3-8) ---------------------
            {FS1_T, FS2_T, FS2_BL,  2},   // upper left
            {FS1_T, FS2_BL, FS1_BL, 2},   // upper left outer
            {FS1_T, FS1_BR, FS2_T,  3},   // upper right
            {FS1_BR, FS2_BR, FS2_T, 3},   // upper right outer
            {FS1_BL, FS2_BL, FS2_BR, 4},   // lower
            {FS1_BL, FS2_BR, FS1_BR, 4},   // lower outer

            // --- Fuselage section FS2→FS3 (indices 9-14) --------------------
            {FS2_T, FS3_T, FS3_BL,  2},   // upper left
            {FS2_T, FS3_BL, FS2_BL, 2},
            {FS2_T, FS2_BR, FS3_T,  3},   // upper right
            {FS2_BR, FS3_BR, FS3_T, 3},
            {FS2_BL, FS3_BL, FS3_BR, 4},   // lower
            {FS2_BL, FS3_BR, FS2_BR, 4},

            // --- Fuselage section FS3→FS4 (indices 15-20) -------------------
            {FS3_T, FS4_T, FS4_BL,  2},   // upper left
            {FS3_T, FS4_BL, FS3_BL, 2},
            {FS3_T, FS3_BR, FS4_T,  3},   // upper right
            {FS3_BR, FS4_BR, FS4_T, 3},
            {FS3_BL, FS4_BL, FS4_BR, 4},   // lower
            {FS3_BL, FS4_BR, FS3_BR, 4},

            // --- Rear cap (index 21) ----------------------------------------
            {FS4_T, FS4_BR, FS4_BL, 5},

            // --- Canopy (indices 22-25) -------------------------------------
            {CAN_F, CAN_T, FS2_T,  6},   // windshield
            {CAN_T, CAN_R, FS2_T,  6},   // canopy top
            {CAN_R, FS3_T, FS2_T,  6},   // rear window
            {FS1_T, CAN_F, FS2_T,  6},   // canopy base front

            // --- Left wing (indices 26-31) ----------------------------------
            {LW_RF_T, LW_T_T, LW_RR_T, 7},   // top surface
            {LW_RF_B, LW_RR_B, LW_T_B, 8},   // bottom surface
            {LW_RF_T, LW_T_T, LW_T_B,  9},   // leading edge
            {LW_RF_T, LW_T_B, LW_RF_B, 9},
            {LW_T_T,  LW_RR_T, LW_RR_B,9},   // trailing edge
            {LW_T_T,  LW_RR_B, LW_T_B, 9},

            // --- Right wing (indices 32-37) ---------------------------------
            {RW_RF_T, RW_RR_T, RW_T_T, 7},   // top surface
            {RW_RF_B, RW_T_B, RW_RR_B, 8},   // bottom surface
            {RW_RF_T, RW_T_B, RW_T_T,  9},   // leading edge
            {RW_RF_T, RW_RF_B, RW_T_B, 9},
            {RW_T_T,  RW_RR_B, RW_RR_T,9},   // trailing edge
            {RW_T_T,  RW_T_B,  RW_RR_B,9},

            // --- Left engine nacelle (indices 38-39) ------------------------
            {FS4_BL, ENG_L_F, ENG_L_B,10},   // exhaust tube
            {FS4_BL, ENG_L_B, FS4_T,  10},   // top of engine

            // --- Right engine nacelle (indices 40-41) -----------------------
            {FS4_BR, ENG_R_B, ENG_R_F,10},
            {FS4_BR, FS4_T,   ENG_R_B,10},

            // --- Vertical fin (indices 42-44) -------------------------------
            {FIN_F, FIN_T, FIN_R,  11},   // fin body
            {FIN_F, FIN_R, FS3_T,  11},   // fin base front
            {FIN_R, FS4_T, FS3_T,  11},   // fin base rear

            // --- Wing root connecting triangles (indices 45-48) -------------
            {LW_RF_T, FS2_BL, LW_RR_T, 12}, // left wing root forward
            {LW_RR_T, FS2_BL, FS3_BL,  12}, // left wing root rear
            {RW_RF_T, RW_RR_T, FS2_BR, 12}, // right wing root forward
            {RW_RR_T, FS3_BR, FS2_BR,  12}, // right wing root rear

            // --- Engine exhaust faces (indices 49-50) -----------------------
            {ENG_L_B, ENG_R_B, FS4_T,  13}, // exhaust glow rear
            {ENG_L_B, FS4_BL,  FS4_BR, 13}, // exhaust glow bottom
        };

        // ---- COLORS ---------------------------------------------------------
        float[][] triColors = {
            // 0 - nose upper:       red
            {0.75f, 0.08f, 0.08f},
            // 1 - nose lower:       dark red
            {0.35f, 0.04f, 0.04f},
            // 2 - fuselage upper:   light grey-blue (top lighting)
            {0.55f, 0.58f, 0.62f},
            // 3 - fuselage upper right:   slightly darker for asymmetry hint
            {0.50f, 0.53f, 0.58f},
            // 4 - fuselage lower:   dark grey (shadow)
            {0.18f, 0.20f, 0.22f},
            // 5 - rear cap:         dark grey
            {0.22f, 0.24f, 0.28f},
            // 6 - canopy:           dark blue glass
            {0.08f, 0.12f, 0.45f},
            // 7 - wing top:         medium-light grey
            {0.48f, 0.50f, 0.53f},
            // 8 - wing bottom:      dark grey
            {0.16f, 0.18f, 0.20f},
            // 9 - wing edges:       medium grey
            {0.30f, 0.32f, 0.35f},
            //10 - engine nacelles:  dark metallic
            {0.25f, 0.26f, 0.28f},
            //11 - vertical fin:     medium grey
            {0.40f, 0.42f, 0.45f},
            //12 - wing root join:   dark accent
            {0.20f, 0.22f, 0.25f},
            //13 - exhaust glow:     bright orange
            {1.00f, 0.55f, 0.00f},
        };

        float[] result = new float[tris.length * 18];
        int idx = 0;
        for (int[] tri : tris) {
            float[] col = triColors[tri[3]];
            for (int vi = 0; vi < 3; vi++) {
                Vector3f pos = v[tri[vi]];
                result[idx++] = pos.x;
                result[idx++] = pos.y;
                result[idx++] = pos.z;
                result[idx++] = col[0];
                result[idx++] = col[1];
                result[idx++] = col[2];
            }
        }
        return result;
    }
}
