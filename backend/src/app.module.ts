import { Module } from '@nestjs/common';
import { JwtModule } from '@nestjs/jwt';
import { PrismaModule } from './prisma/prisma.module';
import { HealthController } from './health.controller';
import { AuthModule } from './modules/auth/auth.module';
import { UsersModule } from './modules/users/users.module';
import { DevicesModule } from './modules/devices/devices.module';
import { UnlocksModule } from './modules/unlocks/unlocks.module';
import { AdminModule } from './modules/admin/admin.module';

@Module({
  controllers: [HealthController],
  imports: [
    JwtModule.register({
      global: true,
      secret: process.env.JWT_SECRET || 'dev-secret',
      signOptions: { expiresIn: process.env.JWT_EXPIRES_IN || '30d' },
    }),
    PrismaModule,
    AuthModule,
    UsersModule,
    DevicesModule,
    UnlocksModule,
    AdminModule,
  ],
})
export class AppModule {}
