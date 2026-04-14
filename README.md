<div align="center">
  <h1>🔥 Stackable Furnace</h1>
  <p align="center">
    <b>A multi-tier smelting system for Fabric 1.20.6.</b><br>
  </p>
</div>

<hr>

<h2 align="center">🚀 Overview</h2>
<p align="justify">
  <b>Stackable Furnace</b> redefines the traditional smelting process by introducing a scalable block system. Instead of crafting multiple furnaces, you upgrade a single block through tiers, unlocking more smelting lines and increasing your industrial capacity within a single block space.
</p>

<h2 align="center">Key Features</h2>

<div align="center">
  <table>
    <tr>
      <td><b>Multi-Tier Progression</b></td>
      <td><b>Global Turbo Boost</b></td>
    </tr>
    <tr>
      <td>Start at Tier 1 and upgrade up to Tier 4 using the furnace block itself. The cost is progressive (Tier 2: 1 block, Tier 3: 2 blocks, Tier 4: 3 blocks).</td>
      <td>A dedicated Turbo Slot (Slot 13) allows you to insert high-quality fuels to provide a global speed boost to all active smelting lines.</td>
    </tr>
  </table>
</div>

<br>

<div align="center">
  <table>
    <tr>
      <td><b>Custom Code-Based GUI</b></td>
      <td><b>Container Compatibility</b></td>
    </tr>
    <tr>
      <td>A sleek, fully programmatic interface showing real-time progress for every active tier, eliminating the need for external textures.</td>
      <td>Full support for <code>RecipeRemainder</code>. Lava buckets return empty buckets, and fluid tanks from other mods are fully compatible.</td>
    </tr>
  </table>
</div>

<hr>

<h2 align="center">🖥️ Interface & Usage</h2>
<p align="justify">
  The Stackable Furnace features a unique GUI that displays the status of each tier. As you upgrade the block, new slots are unlocked.
</p>

<div align="center">
  <img src="figuras/gui_exemplo.png" width="500" alt="GUI Example">
  <p><i>Example of the Custom GUI with Turbo Bar and Tier Progress.</i></p>
</div>

<h3>The Turbo System</h3>
<p align="justify">
  Insert fuel into the <b>Turbo Slot</b> to accelerate all current operations. The boost multiplier is calculated using the square root of the item's base burn time, ensuring that while powerful fuels provide a massive edge, the game remains balanced.
</p>

<hr>

<h2 align="center">⚙️ Automation & Logistics</h2>
<p align="justify">
  Designed for industrial modpacks, the Stackable Furnace supports full automation via Hoppers and Pipes. The I/O is strictly mapped by face:
</p>

<div align="center">
  <table>
    <thead>
      <tr style="background-color: #f2f2f2;">
        <th>Face</th>
        <th>Function</th>
        <th>Description</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <td><b>Top</b></td>
        <td>Input</td>
        <td>Inserts materials into the active smelting lines.</td>
      </tr>
      <tr>
        <td><b>Sides</b></td>
        <td>Fuel</td>
        <td>Feeds fuel into the active burn slots.</td>
      </tr>
      <tr>
        <td><b>Bottom</b></td>
        <td>Output</td>
        <td>Extracts the finished smelted items.</td>
      </tr>
    </tbody>
  </table>
</div>

<hr>

<h2 align="center">🛠️ Installation</h2>
<p align="justify">
  To use Stackable Furnace, ensure you have the following installed:
</p>
<ul>
  <li><b>Fabric Loader</b> (for Minecraft 1.20.6)</li>
  <li><b>Fabric API</b></li>
</ul>

<p align="justify">
  Simply drop the <code>stackable-furnace.jar</code> into your <code>mods</code> folder and launch the game.
</p>

<hr>

<div align="center">
  <p><b>Version:</b> 1.0.0 | <b>Platform:</b> Fabric 1.20.6</p>
</div>
