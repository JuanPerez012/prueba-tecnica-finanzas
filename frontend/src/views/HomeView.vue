<script setup>
import { RouterLink } from 'vue-router'
</script>

<template>
  <div class="home">
    <p class="home__intro">
      Selecciona qué libro quieres consultar. Cada registro de Productos queda
      vinculado a un cliente existente, y cada Transacción queda vinculada a
      una o dos cuentas existentes.
    </p>

    <div class="home__cards">
      <RouterLink to="/clientes" class="home-card">
        <h2 class="home-card__title">Clientes</h2>
        <p class="home-card__desc">
          Alta, edición y baja de clientes de la entidad: datos personales,
          identificación y fecha de nacimiento.
        </p>
        <span class="home-card__cta">Gestionar clientes →</span>
      </RouterLink>

      <RouterLink to="/productos" class="home-card">
        <h2 class="home-card__title">Productos</h2>
        <p class="home-card__desc">
          Cuentas de ahorro y corrientes: estado, saldo, exención de GMF y el
          cliente propietario de cada cuenta.
        </p>
        <span class="home-card__cta">Gestionar productos →</span>
      </RouterLink>

      <RouterLink to="/transacciones" class="home-card">
        <h2 class="home-card__title">Transacciones</h2>
        <p class="home-card__desc">
          Consignaciones, retiros y transferencias entre cuentas. El saldo se
          actualiza automáticamente y cada movimiento queda registrado de
          forma permanente.
        </p>
        <span class="home-card__cta">Ver transacciones →</span>
      </RouterLink>
    </div>
  </div>
</template>
